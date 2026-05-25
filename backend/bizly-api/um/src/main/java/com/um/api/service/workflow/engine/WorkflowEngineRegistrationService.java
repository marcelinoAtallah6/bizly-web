package com.um.api.service.workflow.engine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.um.api.model.workflow.WorkflowApiEndpoint;
import com.um.api.model.workflow.WorkflowConfig;
import com.um.api.model.workflow.engine.WorkflowDefinition;
import com.um.api.model.workflow.engine.WorkflowStep;
import com.um.api.repository.workflow.WorkflowApiEndpointRepository;
import com.um.api.repository.workflow.WorkflowConfigRepository;
import com.um.api.repository.workflow.engine.WorkflowDefinitionRepository;
import com.um.api.repository.workflow.engine.WorkflowStepRepository;
import com.um.api.service.workflow.WorkflowServiceImpl;
import com.um.exception.ServiceException;

/**
 * Bridges legacy {@code BUSINESS_REGISTRATION} rows in {@code UM_WORKFLOW_CONFIG} with workflow engine
 * pipelines so registration appears in the engine grid and disable/delete stay in sync.
 */
@Service
public class WorkflowEngineRegistrationService {

	private static final int REGISTRATION_VERSION = 1;

	public static final String LEGACY_BUILTIN_KEY = WorkflowServiceImpl.BUILTIN_BUSINESS_REGISTRATION;
	public static final String ENGINE_ACTION_CODE = "BUSINESS_REGISTRATION";
	public static final String SCREEN_ONBOARDING = "Onboarding";
	public static final String ACTION_BUSINESS_REGISTRATION = "Business registration";

	@Autowired
	private WorkflowConfigRepository configRepository;
	@Autowired
	private WorkflowDefinitionRepository definitionRepository;
	@Autowired
	private WorkflowStepRepository stepRepository;
	@Autowired
	private WorkflowApiEndpointRepository endpointRepository;
	@Autowired
	private WorkflowEngineApprovalSyncService approvalSyncService;
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Imports the legacy global registration config into {@code UM_WORKFLOW_DEFINITION} when missing.
	 */
	@Transactional
	public void ensureEnginePipelineImported(String username) {
		if (findRegistrationDefinitionScope().isPresent()) {
			return;
		}
		Optional<WorkflowConfig> legacy = configRepository.findFirstByBuiltInKeyAndBusinessIdIsNull(LEGACY_BUILTIN_KEY);
		if (legacy.isEmpty()) {
			return;
		}
		ensureRegistrationEndpoint();

		WorkflowConfig cfg = legacy.get();
		WorkflowDefinition def = new WorkflowDefinition();
		def.setActionCode(ENGINE_ACTION_CODE);
		def.setVersionNo(REGISTRATION_VERSION);
		def.setStatus(cfg.isHasWorkflow() ? "PUBLISHED" : "DISABLED");
		def.setBusinessId(null);
		def.setDisplayName("Business registration approval");
		def.setNotes("Imported from UM_WORKFLOW_CONFIG." + LEGACY_BUILTIN_KEY);
		def.setWorkflowType("APPROVAL_GATEWAY");
		def.setRoleRestricted(false);
		def.setAllowChildRoleInherit(false);
		def.setCreatedBy(username);
		def.setCreatedAt(LocalDateTime.now());
		def.setUpdatedAt(LocalDateTime.now());
		if (cfg.isHasWorkflow()) {
			def.setPublishedAt(LocalDateTime.now());
			def.setPublishedBy(username);
		}
		def = definitionRepository.save(def);

		WorkflowStep step = new WorkflowStep();
		step.setDefinitionId(def.getId());
		step.setStepOrder(10);
		step.setStepType(WorkflowEngineOrchestratorService.STEP_APPROVAL);
		step.setActive(true);
		step.setDisplayLabel("Business registration");
		step.setTaskKey("APPROVAL_MANUAL");
		step.setConfigJson(buildApprovalConfigJson(cfg));
		stepRepository.save(step);

		if (cfg.isHasWorkflow()) {
			approvalSyncService.syncOnPublish(def,
					stepRepository.findByDefinitionIdAndIsActiveOrderByStepOrderAsc(def.getId(), 1), username);
			cfg.setBuiltInKey(LEGACY_BUILTIN_KEY);
			cfg.setHasWorkflow(false);
			configRepository.save(cfg);
		}
	}

	public boolean isRegistrationDefinition(WorkflowDefinition def) {
		return def != null && ENGINE_ACTION_CODE.equalsIgnoreCase(def.getActionCode());
	}

	public boolean isRegistrationConfig(WorkflowConfig cfg) {
		if (cfg == null) {
			return false;
		}
		if (LEGACY_BUILTIN_KEY.equalsIgnoreCase(cfg.getBuiltInKey())) {
			return true;
		}
		if (SCREEN_ONBOARDING.equalsIgnoreCase(cfg.getScreenName())
				&& ACTION_BUSINESS_REGISTRATION.equalsIgnoreCase(cfg.getActionName())
				&& cfg.getBusinessId() == null) {
			return true;
		}
		return isRegistrationEngineBuiltInKey(cfg.getBuiltInKey());
	}

	/**
	 * Active registration approval config: published engine pipeline only, else legacy when no engine row exists.
	 */
	public Optional<WorkflowConfig> resolveActiveRegistrationConfig() {
		Optional<WorkflowDefinition> published = definitionRepository.findBestPublished(ENGINE_ACTION_CODE, null);
		if (published.isPresent()) {
			return findActiveSyncedConfig(published.get().getId());
		}
		if (findRegistrationDefinitionScope().isPresent()) {
			return Optional.empty();
		}
		return configRepository.findFirstByBuiltInKeyAndBusinessIdIsNull(LEGACY_BUILTIN_KEY)
				.filter(WorkflowConfig::isHasWorkflow);
	}

	@Transactional
	public void onRegistrationPipelineDisabled(Long definitionId) {
		deactivateAllRegistrationGatewayConfigs();
	}

	private Optional<WorkflowDefinition> findRegistrationDefinitionScope() {
		return definitionRepository.findFirstByActionCodeIgnoreCaseAndVersionNoAndBusinessIdIsNull(ENGINE_ACTION_CODE,
				REGISTRATION_VERSION);
	}

	private Optional<WorkflowConfig> findActiveSyncedConfig(Long definitionId) {
		String prefix = WorkflowEngineApprovalSyncService.BUILT_IN_PREFIX + definitionId + ":STEP:";
		for (WorkflowConfig cfg : configRepository.findByBuiltInKeyStartingWith(prefix)) {
			if (cfg.isHasWorkflow()) {
				return Optional.of(cfg);
			}
		}
		return Optional.empty();
	}

	private void deactivateAllRegistrationGatewayConfigs() {
		configRepository.findFirstByBuiltInKeyAndBusinessIdIsNull(LEGACY_BUILTIN_KEY).ifPresent(this::deactivateConfig);
		configRepository
				.findByScreenNameIgnoreCaseAndActionNameIgnoreCase(SCREEN_ONBOARDING, ACTION_BUSINESS_REGISTRATION)
				.stream()
				.filter(c -> c.getBusinessId() == null)
				.forEach(this::deactivateConfig);
		for (WorkflowDefinition def : definitionRepository.findByActionCodeIgnoreCaseAndBusinessIdIsNull(
				ENGINE_ACTION_CODE)) {
			for (WorkflowConfig cfg : configRepository
					.findByBuiltInKeyStartingWith(WorkflowEngineApprovalSyncService.BUILT_IN_PREFIX + def.getId()
							+ ":")) {
				deactivateConfig(cfg);
			}
		}
	}

	private void deactivateConfig(WorkflowConfig cfg) {
		if (cfg != null && cfg.isHasWorkflow()) {
			cfg.setHasWorkflow(false);
			configRepository.save(cfg);
		}
	}

	private boolean isRegistrationEngineBuiltInKey(String builtInKey) {
		if (builtInKey == null || !builtInKey.startsWith(WorkflowEngineApprovalSyncService.BUILT_IN_PREFIX)) {
			return false;
		}
		int colon = builtInKey.indexOf(":STEP:");
		if (colon < 1) {
			return false;
		}
		try {
			long defId = Long.parseLong(builtInKey.substring(WorkflowEngineApprovalSyncService.BUILT_IN_PREFIX.length(),
					colon));
			return definitionRepository.findById(defId).map(this::isRegistrationDefinition).orElse(false);
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	private void ensureRegistrationEndpoint() {
		if (endpointRepository.findFirstByEngineActionCodeIgnoreCase(ENGINE_ACTION_CODE).isPresent()) {
			return;
		}
		WorkflowApiEndpoint ep = new WorkflowApiEndpoint();
		ep.setServiceKey("um");
		ep.setHttpMethod("INTERNAL");
		ep.setPathAntPattern("/um/internal/trigger/BUSINESS_REGISTRATION");
		ep.setScreenRoute(SCREEN_ONBOARDING);
		ep.setActionCode("EVENT");
		ep.setEngineActionCode(ENGINE_ACTION_CODE);
		ep.setDisplayName("Business registration (onboarding)");
		ep.setDescription("Onboarding approval — not an HTTP gateway deferral.");
		ep.setTriggerKind("MANUAL");
		ep.setTriggerSource("DOMAIN");
		ep.setPriority(5);
		ep.setActive(true);
		ep.setCreatedAt(LocalDateTime.now());
		endpointRepository.save(ep);
	}

	private String buildApprovalConfigJson(WorkflowConfig cfg) {
		try {
			com.fasterxml.jackson.databind.node.ObjectNode root = objectMapper.createObjectNode();
			root.put("screenRoute", SCREEN_ONBOARDING);
			root.put("actionName", ACTION_BUSINESS_REGISTRATION);
			root.put("userOverride", cfg.getMakerUsersJson() != null && !cfg.getMakerUsersJson().isBlank());
			if (cfg.getMakerRolesJson() != null && !cfg.getMakerRolesJson().isBlank()) {
				root.set("makerRoles", objectMapper.readTree(cfg.getMakerRolesJson()));
			}
			if (cfg.getMakerUsersJson() != null && !cfg.getMakerUsersJson().isBlank()) {
				root.set("makerUsers", objectMapper.readTree(cfg.getMakerUsersJson()));
			}
			if (cfg.getCheckerRolesJson() != null && !cfg.getCheckerRolesJson().isBlank()) {
				JsonNode checker = objectMapper.readTree(cfg.getCheckerRolesJson());
				if (checker.isArray() && checker.size() > 0 && checker.get(0).isArray()) {
					root.set("checkerRoleTiers", checker);
				} else {
					root.set("checkerRoleTiers", objectMapper.createArrayNode().add(checker));
				}
			}
			return objectMapper.writeValueAsString(root);
		} catch (Exception ex) {
			throw new ServiceException("Could not import registration approval settings.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
