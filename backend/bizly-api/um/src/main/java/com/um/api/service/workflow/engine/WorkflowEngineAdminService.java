package com.um.api.service.workflow.engine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.um.api.dto.menu.MenuPermissionRouteRow;
import com.um.api.dto.menu.PermissionMetadataResponse;
import com.um.api.dto.workflow.engine.ApplicationActionTargetDto;
import com.um.api.dto.workflow.engine.CreateWorkflowDefinitionRequest;
import com.um.api.dto.workflow.engine.EmailTemplateKeyDto;
import com.um.api.dto.workflow.engine.SaveWorkflowStepsRequest;
import com.um.api.dto.workflow.engine.UpdateWorkflowDefinitionRequest;
import com.um.api.dto.workflow.engine.WorkflowActionDto;
import com.um.api.dto.workflow.engine.WorkflowDefinitionDetailDto;
import com.um.api.dto.workflow.engine.WorkflowDefinitionRowDto;
import com.um.api.dto.workflow.engine.WorkflowStepDto;
import com.um.api.model.workflow.WorkflowApiEndpoint;
import com.um.api.model.workflow.engine.WorkflowDefinition;
import com.um.api.model.workflow.engine.WorkflowStep;
import com.um.api.repository.workflow.WorkflowApiEndpointRepository;
import com.um.api.repository.workflow.engine.WorkflowDefinitionRepository;
import com.um.api.repository.workflow.engine.WorkflowStepRepository;
import com.um.common.ApiMessages;
import com.um.exception.ServiceException;
import com.um.api.service.menu.MenuPermissionMetadataService;
import com.um.api.service.security.MenuPermissionService;
import com.um.security.BusinessContextHolder;

@Service
public class WorkflowEngineAdminService {

	private static final String STATUS_DRAFT = "DRAFT";
	private static final String STATUS_PUBLISHED = "PUBLISHED";
	private static final String STATUS_DISABLED = "DISABLED";

	@Autowired
	private WorkflowDefinitionRepository definitionRepository;
	@Autowired
	private WorkflowStepRepository stepRepository;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private MenuPermissionMetadataService menuPermissionMetadataService;
	@Autowired
	private MenuPermissionService menuPermissionService;
	@Autowired
	private WorkflowApiEndpointRepository workflowApiEndpointRepository;
	@Autowired
	private WorkflowEngineTriggerResolver triggerResolver;
	@Autowired
	private WorkflowEngineApprovalSyncService approvalSyncService;
	@Autowired
	private WorkflowEngineRegistrationService registrationService;

	/** All selectable pipeline triggers from {@code UM_WORKFLOW_API_ENDPOINT} (HTTP + DOMAIN). */
	public List<WorkflowActionDto> listActions() {
		Map<String, String> labelByRoute = buildMenuLabelByRoute();
		return workflowApiEndpointRepository.findByActiveTrueOrderByDisplayNameAscIdAsc().stream()
				.map(ep -> toEndpointActionDto(ep, labelByRoute))
				.sorted(Comparator.comparing(d -> d.getDisplayName() == null ? "" : d.getDisplayName(),
						String.CASE_INSENSITIVE_ORDER))
				.collect(Collectors.toList());
	}

	/**
	 * Flat list of every screen route + action verb the current principal's role(s) may perform.
	 */
	public List<ApplicationActionTargetDto> listApplicationActionTargets() {
		PermissionMetadataResponse meta;
		if (BusinessContextHolder.canBypassTenant()) {
			meta = menuPermissionMetadataService.buildMetadata();
		} else {
			meta = menuPermissionMetadataService
					.buildMetadataForRoleIds(menuPermissionService.resolveAllAssignedRoleIds());
		}
		Map<Long, List<String>> byMenu = meta.getActionsByMenuId() == null ? Map.of() : meta.getActionsByMenuId();
		List<ApplicationActionTargetDto> out = new ArrayList<>();
		for (MenuPermissionRouteRow row : meta.getMenus() == null ? List.<MenuPermissionRouteRow>of() : meta.getMenus()) {
			if (row.getRoute() == null || row.getRoute().isBlank()) {
				continue;
			}
			Long mid = row.getMenuId();
			List<String> actions = mid == null ? List.of() : byMenu.getOrDefault(mid, List.of());
			String label = row.getMenuPath() != null && !row.getMenuPath().isBlank() ? row.getMenuPath() : row.getRoute();
			for (String action : actions) {
				if (action == null || action.isBlank()) {
					continue;
				}
				ApplicationActionTargetDto dto = new ApplicationActionTargetDto();
				dto.setScreenRoute(row.getRoute().trim());
				dto.setMenuLabel(label);
				dto.setActionCode(action.trim().toUpperCase());
				out.add(dto);
			}
		}
		out.sort((a, b) -> {
			int c = (a.getMenuLabel() == null ? "" : a.getMenuLabel())
					.compareToIgnoreCase(b.getMenuLabel() == null ? "" : b.getMenuLabel());
			if (c != 0) {
				return c;
			}
			return (a.getActionCode() == null ? "" : a.getActionCode())
					.compareToIgnoreCase(b.getActionCode() == null ? "" : b.getActionCode());
		});
		return out;
	}

	public List<EmailTemplateKeyDto> listEmailTemplates() {
		return jdbcTemplate.query(
				"SELECT TEMPLATE_KEY, SUBJECT_TMPL FROM UM.NOTIF_EMAIL_TEMPLATE WHERE ACTIVE_IND = 'Y' ORDER BY TEMPLATE_KEY",
				(rs, i) -> new EmailTemplateKeyDto(rs.getString("TEMPLATE_KEY"), rs.getString("SUBJECT_TMPL")));
	}

	public List<WorkflowDefinitionRowDto> listDefinitions() {
		registrationService.ensureEnginePipelineImported("system-import");
		repairPublishedPipelineTriggers();
		Long scopeBusinessId = resolveScopeBusinessIdForList();
		List<WorkflowDefinition> defs = definitionRepository.findAll().stream()
				.filter(d -> matchesListScope(d, scopeBusinessId))
				.sorted(Comparator.comparing(WorkflowDefinition::getActionCode)
						.thenComparing(WorkflowDefinition::getVersionNo, Comparator.reverseOrder()))
				.collect(Collectors.toList());
		Map<String, String> actionLabels = buildActionLabelMap();
		return defs.stream().map(d -> toRowDto(d, actionLabels)).collect(Collectors.toList());
	}

	public WorkflowDefinitionDetailDto getDefinition(Long id) {
		WorkflowDefinition def = loadDefinitionForCaller(id);
		Map<String, String> actionLabels = buildActionLabelMap();
		WorkflowDefinitionDetailDto detail = new WorkflowDefinitionDetailDto();
		copyRow(toRowDto(def, actionLabels), detail);
		List<WorkflowStep> steps = stepRepository.findByDefinitionIdOrderByStepOrderAsc(def.getId());
		detail.setSteps(steps.stream().map(this::toStepDto).collect(Collectors.toList()));
		return detail;
	}

	@Transactional
	public Long createDefinition(String username, CreateWorkflowDefinitionRequest req) {
		assertActionExists(req.getActionCode());
		String canonicalAction = triggerResolver.normalizeTriggerCode(req.getActionCode());
		Long businessId = resolveBusinessIdForWrite(req.getBusinessId());
		int nextVersion = nextVersionNo(canonicalAction, businessId);

		WorkflowDefinition def = new WorkflowDefinition();
		def.setActionCode(canonicalAction);
		def.setVersionNo(nextVersion);
		def.setStatus(STATUS_DRAFT);
		def.setBusinessId(businessId);
		def.setDisplayName(trimOr(req.getDisplayName(), req.getActionCode()));
		def.setNotes(req.getNotes());
		def.setCreatedBy(username);
		def.setCreatedAt(LocalDateTime.now());
		def.setUpdatedAt(LocalDateTime.now());
		def.setWorkflowType(normalizeWorkflowType(req.getWorkflowType()));
		def.setRoleRestricted(false);
		def.setAllowChildRoleInherit(false);
		return definitionRepository.save(def).getId();
	}

	@Transactional
	public void updateDefinition(String username, UpdateWorkflowDefinitionRequest req) {
		WorkflowDefinition def = loadDefinitionForCaller(req.getId());
		assertEditable(def);
		if (req.getDisplayName() != null) {
			def.setDisplayName(req.getDisplayName().trim());
		}
		if (req.getNotes() != null) {
			def.setNotes(req.getNotes());
		}
		if (req.getWorkflowType() != null) {
			def.setWorkflowType(normalizeWorkflowType(req.getWorkflowType()));
		}
		if (req.getRoleRestricted() != null) {
			def.setRoleRestricted(req.getRoleRestricted());
		}
		if (req.getAllowChildRoleInherit() != null) {
			def.setAllowChildRoleInherit(req.getAllowChildRoleInherit());
		}
		if (req.getRoleCodesJson() != null) {
			def.setRoleCodesJson(req.getRoleCodesJson().isBlank() ? null : req.getRoleCodesJson().trim());
		}
		def.setUpdatedAt(LocalDateTime.now());
		definitionRepository.save(def);
	}

	@Transactional
	public void saveSteps(String username, SaveWorkflowStepsRequest req) {
		WorkflowDefinition def = loadDefinitionForCaller(req.getDefinitionId());
		assertEditable(def);
		stepRepository.deleteByDefinitionId(def.getId());
		int order = 10;
		for (WorkflowStepDto dto : req.getSteps()) {
			validateStep(dto);
			WorkflowStep step = new WorkflowStep();
			step.setDefinitionId(def.getId());
			step.setStepOrder(dto.getStepOrder() != null ? dto.getStepOrder() : order);
			step.setStepType(dto.getStepType().trim().toUpperCase());
			step.setActive(dto.isActive());
			step.setConfigJson(normalizeConfigJson(dto));
			step.setWorkflowConfigId(dto.getWorkflowConfigId());
			step.setTaskKey(trimOrNull(dto.getTaskKey()));
			step.setDisplayLabel(trimOrNull(dto.getDisplayLabel()));
			stepRepository.save(step);
			order += 10;
		}
		def.setUpdatedAt(LocalDateTime.now());
		definitionRepository.save(def);
		if (STATUS_PUBLISHED.equals(def.getStatus())) {
			List<WorkflowStep> steps = stepRepository.findByDefinitionIdAndIsActiveOrderByStepOrderAsc(def.getId(), 1);
			alignPipelineTriggerFromSteps(def, steps);
			definitionRepository.save(def);
			syncApprovalForDefinition(def, username);
		}
	}

	@Transactional
	public void publish(String username, Long id) {
		WorkflowDefinition def = loadDefinitionForCaller(id);
		if (!isEditableStatus(def.getStatus())) {
			throw new ServiceException("This pipeline cannot be published.", HttpStatus.BAD_REQUEST);
		}
		List<WorkflowStep> steps = stepRepository.findByDefinitionIdAndIsActiveOrderByStepOrderAsc(def.getId(), 1);
		if (steps.isEmpty()) {
			throw new ServiceException("Add at least one active step before publishing.", HttpStatus.BAD_REQUEST);
		}
		alignPipelineTriggerFromSteps(def, steps);
		approvalSyncService.syncOnPublish(def, steps, username);
		disableOtherPublished(def);
		boolean firstPublish = def.getPublishedAt() == null;
		def.setStatus(STATUS_PUBLISHED);
		if (firstPublish) {
			def.setPublishedAt(LocalDateTime.now());
			def.setPublishedBy(username);
		}
		def.setUpdatedAt(LocalDateTime.now());
		definitionRepository.save(def);
	}

	@Transactional
	public void disable(String username, Long id) {
		WorkflowDefinition def = loadDefinitionForCaller(id);
		approvalSyncService.deactivateAllApprovalConfigsForDefinition(def.getId());
		if (registrationService.isRegistrationDefinition(def)) {
			registrationService.onRegistrationPipelineDisabled(def.getId());
		}
		def.setStatus(STATUS_DISABLED);
		def.setUpdatedAt(LocalDateTime.now());
		definitionRepository.save(def);
	}

	@Transactional
	public void deleteDraft(Long id) {
		WorkflowDefinition def = loadDefinitionForCaller(id);
		approvalSyncService.deactivateAllApprovalConfigsForDefinition(def.getId());
		if (registrationService.isRegistrationDefinition(def)) {
			registrationService.onRegistrationPipelineDisabled(def.getId());
		}
		stepRepository.deleteByDefinitionId(def.getId());
		definitionRepository.delete(def);
	}

	private void assertEditable(WorkflowDefinition def) {
		if (!isEditableStatus(def.getStatus())) {
			throw new ServiceException("This pipeline cannot be edited.", HttpStatus.BAD_REQUEST);
		}
	}

	private static boolean isEditableStatus(String status) {
		return STATUS_DRAFT.equals(status) || STATUS_PUBLISHED.equals(status) || STATUS_DISABLED.equals(status);
	}

	private void syncApprovalForDefinition(WorkflowDefinition def, String username) {
		List<WorkflowStep> steps = stepRepository.findByDefinitionIdAndIsActiveOrderByStepOrderAsc(def.getId(), 1);
		if (!steps.isEmpty()) {
			approvalSyncService.syncOnPublish(def, steps, username);
		}
	}

	/**
	 * Keeps the published pipeline trigger ({@code EP:{id}}) aligned with the gateway approval step endpoint
	 * so HTTP completion and notifications resolve the same definition.
	 */
	private void alignPipelineTriggerFromSteps(WorkflowDefinition def, List<WorkflowStep> steps) {
		for (WorkflowStep step : steps) {
			if (!WorkflowEngineOrchestratorService.STEP_APPROVAL.equalsIgnoreCase(step.getStepType())
					|| !step.isActive()) {
				continue;
			}
			if (step.getConfigJson() == null || step.getConfigJson().isBlank()) {
				continue;
			}
			try {
				JsonNode cfg = objectMapper.readTree(step.getConfigJson());
				JsonNode epNode = cfg.get("endpointId");
				if (epNode != null && epNode.isNumber()) {
					long endpointId = epNode.asLong();
					if (endpointId > 0) {
						def.setActionCode(WorkflowEngineActionCodes.forEndpoint(endpointId));
						return;
					}
				}
			} catch (Exception ignored) {
				// keep existing actionCode
			}
		}
		String normalized = triggerResolver.normalizeTriggerCode(def.getActionCode());
		if (normalized != null && !normalized.isBlank()) {
			def.setActionCode(normalized);
		}
	}

	/** Fixes legacy rows still keyed as USER_CREATED_SUCCESS instead of EP:{id}. */
	private void repairPublishedPipelineTriggers() {
		for (WorkflowDefinition def : definitionRepository.findAll()) {
			if (!STATUS_PUBLISHED.equalsIgnoreCase(def.getStatus())) {
				continue;
			}
			String before = def.getActionCode();
			List<WorkflowStep> steps = stepRepository.findByDefinitionIdAndIsActiveOrderByStepOrderAsc(def.getId(), 1);
			alignPipelineTriggerFromSteps(def, steps);
			if (before != null && !before.equals(def.getActionCode())) {
				def.setUpdatedAt(LocalDateTime.now());
				definitionRepository.save(def);
			}
		}
	}

	private void disableOtherPublished(WorkflowDefinition publishing) {
		List<WorkflowDefinition> siblings = definitionRepository.findAll().stream()
				.filter(d -> Objects.equals(d.getActionCode(), publishing.getActionCode()))
				.filter(d -> Objects.equals(d.getBusinessId(), publishing.getBusinessId()))
				.filter(d -> STATUS_PUBLISHED.equals(d.getStatus()))
				.filter(d -> !Objects.equals(d.getId(), publishing.getId()))
				.collect(Collectors.toList());
		for (WorkflowDefinition d : siblings) {
			approvalSyncService.deactivateAllApprovalConfigsForDefinition(d.getId());
			d.setStatus(STATUS_DISABLED);
			d.setUpdatedAt(LocalDateTime.now());
			definitionRepository.save(d);
		}
	}

	private static final java.util.Set<String> ALLOWED_STEP_TYPES = java.util.Set.of(
			WorkflowEngineOrchestratorService.STEP_NOTIFICATION,
			WorkflowEngineOrchestratorService.STEP_APPROVAL,
			"HTTP_TASK", "HTTP_POLL", "SYSTEM");

	private void validateStep(WorkflowStepDto dto) {
		String type = dto.getStepType() != null ? dto.getStepType().trim().toUpperCase() : "";
		if (!ALLOWED_STEP_TYPES.contains(type)) {
			throw new ServiceException("Unsupported step type: " + type, HttpStatus.BAD_REQUEST);
		}
		if (WorkflowEngineOrchestratorService.STEP_NOTIFICATION.equals(type)) {
			try {
				JsonNode cfg = objectMapper.readTree(dto.getConfigJson());
				String templateKey = cfg.path("templateKey").asText("");
				if (templateKey.isBlank()) {
					throw new ServiceException("Notification step requires templateKey.", HttpStatus.BAD_REQUEST);
				}
			} catch (ServiceException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new ServiceException("Invalid notification step JSON.", HttpStatus.BAD_REQUEST);
			}
		}
		if (WorkflowEngineOrchestratorService.STEP_APPROVAL.equals(type)) {
			validateApprovalStepDto(dto);
		}
	}

	private void validateApprovalStepDto(WorkflowStepDto dto) {
		try {
			JsonNode cfg = objectMapper.readTree(dto.getConfigJson());
			String screen = cfg.path("screenRoute").asText("").trim();
			String action = cfg.path("actionName").asText("").trim();
			if (screen.isBlank() || action.isBlank()) {
				throw new ServiceException("Approval step requires an application action (screen + verb).",
						HttpStatus.BAD_REQUEST);
			}
			boolean userOverride = cfg.path("userOverride").asBoolean(false);
			boolean hasEndpoint = cfg.has("endpointId") && !cfg.get("endpointId").isNull();
			if (!hasEndpoint && (screen.isBlank() || action.isBlank())) {
				throw new ServiceException("Approval step requires a gateway API endpoint.",
						HttpStatus.BAD_REQUEST);
			}
			if (!hasCheckerOnFirstLevel(cfg, userOverride)) {
				throw new ServiceException("Approval step requires at least one checker on level 1.",
						HttpStatus.BAD_REQUEST);
			}
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ServiceException("Invalid approval step JSON.", HttpStatus.BAD_REQUEST);
		}
	}

	private boolean hasCheckerOnFirstLevel(JsonNode cfg, boolean userOverride) {
		JsonNode tiers = userOverride ? cfg.get("checkerUserTiers") : cfg.get("checkerRoleTiers");
		if (tiers != null && tiers.isArray() && tiers.size() > 0) {
			JsonNode first = tiers.get(0);
			if (first != null && first.isArray() && first.size() > 0) {
				return true;
			}
		}
		if (!userOverride) {
			JsonNode legacy = cfg.get("roleNames");
			return legacy != null && legacy.isArray() && legacy.size() > 0;
		}
		return false;
	}

	private String normalizeConfigJson(WorkflowStepDto dto) {
		if (dto.getConfigJson() == null || dto.getConfigJson().isBlank()) {
			return "{}";
		}
		return dto.getConfigJson().trim();
	}

	private int nextVersionNo(String actionCode, Long businessId) {
		return definitionRepository.findAll().stream()
				.filter(d -> actionCode.equalsIgnoreCase(d.getActionCode()))
				.filter(d -> Objects.equals(d.getBusinessId(), businessId))
				.mapToInt(d -> d.getVersionNo() != null ? d.getVersionNo() : 0)
				.max().orElse(0) + 1;
	}

	private void assertActionExists(String actionCode) {
		if (triggerResolver.isKnownTrigger(actionCode)) {
			return;
		}
		throw new ServiceException("Unknown trigger action: " + actionCode, HttpStatus.BAD_REQUEST);
	}

	private Map<String, String> buildActionLabelMap() {
		Map<String, String> labelByRoute = buildMenuLabelByRoute();
		Map<String, String> labels = new LinkedHashMap<>();
		for (WorkflowApiEndpoint ep : workflowApiEndpointRepository.findByActiveTrueOrderByDisplayNameAscIdAsc()) {
			String display = catalogDisplayName(ep, labelByRoute);
			labels.put(WorkflowEngineActionCodes.forEndpoint(ep.getId()), display);
			if (ep.getEngineActionCode() != null && !ep.getEngineActionCode().isBlank()) {
				labels.put(ep.getEngineActionCode().trim().toUpperCase(Locale.ROOT), display);
			}
		}
		return labels;
	}

	private Map<String, String> buildMenuLabelByRoute() {
		PermissionMetadataResponse meta = menuPermissionMetadataService.buildMetadata();
		Map<String, String> m = new LinkedHashMap<>();
		for (MenuPermissionRouteRow r : meta.getMenus() == null ? List.<MenuPermissionRouteRow>of() : meta.getMenus()) {
			if (r.getRoute() == null || r.getRoute().isBlank()) {
				continue;
			}
			String key = r.getRoute().trim().toLowerCase(Locale.ROOT);
			String label = r.getMenuPath() != null && !r.getMenuPath().isBlank() ? r.getMenuPath() : r.getRoute();
			m.putIfAbsent(key, label);
		}
		return m;
	}

	private static String endpointDisplayName(WorkflowApiEndpoint ep, Map<String, String> labelByRoute) {
		String sr = ep.getScreenRoute() == null ? "" : ep.getScreenRoute().trim();
		String ml = labelByRoute.getOrDefault(sr.toLowerCase(Locale.ROOT), sr);
		String method = ep.getHttpMethod() == null ? "" : ep.getHttpMethod().trim();
		String action = ep.getActionCode() == null ? "" : ep.getActionCode().trim();
		String path = ep.getPathAntPattern() == null ? "" : ep.getPathAntPattern().trim();
		return ml + " · " + action + " (" + method + " " + path + ")";
	}

	private static String buildEndpointDescription(WorkflowApiEndpoint ep) {
		return "API endpoint #" + ep.getId() + " · " + ep.getServiceKey();
	}

	private WorkflowDefinition loadDefinitionForCaller(Long id) {
		WorkflowDefinition def = definitionRepository.findById(id)
				.orElseThrow(() -> new ServiceException("Workflow pipeline not found.", HttpStatus.NOT_FOUND));
		Long scope = resolveScopeBusinessIdForList();
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			if (scope == null) {
				return def;
			}
			if (def.getBusinessId() == null || Objects.equals(def.getBusinessId(), scope)) {
				return def;
			}
			throw new ServiceException("Access denied.", HttpStatus.FORBIDDEN);
		}
		if (def.getBusinessId() == null || !def.getBusinessId().equals(scope)) {
			throw new ServiceException("Access denied.", HttpStatus.FORBIDDEN);
		}
		return def;
	}

	private Long resolveBusinessIdForWrite(Long requested) {
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			return requested != null ? requested : BusinessContextHolder.currentBusinessId();
		}
		return BusinessContextHolder.requireBusinessId();
	}

	private Long resolveScopeBusinessIdForList() {
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			return BusinessContextHolder.currentBusinessId();
		}
		return BusinessContextHolder.requireBusinessId();
	}

	private boolean matchesListScope(WorkflowDefinition d, Long scopeBusinessId) {
		if (BusinessContextHolder.isPortalAdminRoleLevel() && scopeBusinessId == null) {
			return true;
		}
		if (BusinessContextHolder.isPortalAdminRoleLevel()) {
			return d.getBusinessId() == null || Objects.equals(d.getBusinessId(), scopeBusinessId);
		}
		return Objects.equals(d.getBusinessId(), scopeBusinessId);
	}

	private WorkflowDefinitionRowDto toRowDto(WorkflowDefinition d, Map<String, String> actionLabels) {
		WorkflowDefinitionRowDto row = new WorkflowDefinitionRowDto();
		row.setId(d.getId());
		row.setActionCode(d.getActionCode());
		row.setActionDisplayName(actionLabels.getOrDefault(d.getActionCode(), d.getActionCode()));
		row.setVersionNo(d.getVersionNo());
		row.setStatus(d.getStatus());
		row.setBusinessId(d.getBusinessId());
		row.setDisplayName(d.getDisplayName());
		row.setNotes(d.getNotes());
		row.setPublishedAt(d.getPublishedAt());
		row.setCreatedBy(d.getCreatedBy());
		row.setCreatedAt(d.getCreatedAt());
		row.setStepCount(stepRepository.findByDefinitionIdOrderByStepOrderAsc(d.getId()).size());
		row.setWorkflowType(d.getWorkflowType());
		row.setRoleRestricted(d.isRoleRestricted());
		row.setAllowChildRoleInherit(d.isAllowChildRoleInherit());
		row.setRoleCodesJson(d.getRoleCodesJson());
		return row;
	}

	private void copyRow(WorkflowDefinitionRowDto from, WorkflowDefinitionDetailDto to) {
		to.setId(from.getId());
		to.setActionCode(from.getActionCode());
		to.setActionDisplayName(from.getActionDisplayName());
		to.setVersionNo(from.getVersionNo());
		to.setStatus(from.getStatus());
		to.setBusinessId(from.getBusinessId());
		to.setDisplayName(from.getDisplayName());
		to.setNotes(from.getNotes());
		to.setPublishedAt(from.getPublishedAt());
		to.setCreatedBy(from.getCreatedBy());
		to.setCreatedAt(from.getCreatedAt());
		to.setStepCount(from.getStepCount());
		to.setWorkflowType(from.getWorkflowType());
		to.setRoleRestricted(from.isRoleRestricted());
		to.setAllowChildRoleInherit(from.isAllowChildRoleInherit());
		to.setRoleCodesJson(from.getRoleCodesJson());
	}

	private WorkflowStepDto toStepDto(WorkflowStep s) {
		WorkflowStepDto dto = new WorkflowStepDto();
		dto.setId(s.getId());
		dto.setStepOrder(s.getStepOrder());
		dto.setStepType(s.getStepType());
		dto.setActive(s.isActive());
		dto.setConfigJson(s.getConfigJson());
		dto.setWorkflowConfigId(s.getWorkflowConfigId());
		dto.setTaskKey(s.getTaskKey());
		dto.setDisplayLabel(s.getDisplayLabel());
		return dto;
	}

	private WorkflowActionDto toEndpointActionDto(WorkflowApiEndpoint ep, Map<String, String> labelByRoute) {
		WorkflowActionDto dto = new WorkflowActionDto();
		dto.setActionCode(WorkflowEngineActionCodes.forEndpoint(ep.getId()));
		dto.setDisplayName(catalogDisplayName(ep, labelByRoute));
		dto.setDescription(ep.getDescription() != null && !ep.getDescription().isBlank()
				? ep.getDescription()
				: buildEndpointDescription(ep));
		dto.setModuleKey(ep.getServiceKey());
		dto.setTriggerKind(ep.getTriggerKind() != null ? ep.getTriggerKind() : "IMMEDIATE");
		return dto;
	}

	private static String catalogDisplayName(WorkflowApiEndpoint ep, Map<String, String> labelByRoute) {
		if (ep.getDisplayName() != null && !ep.getDisplayName().isBlank()) {
			String base = ep.getDisplayName().trim();
			if ("SCHEDULED".equalsIgnoreCase(ep.getTriggerKind())) {
				return base + " (scheduled)";
			}
			return base;
		}
		if (WorkflowEngineTriggerResolver.TRIGGER_SOURCE_DOMAIN.equalsIgnoreCase(ep.getTriggerSource())) {
			String code = ep.getEngineActionCode() != null ? ep.getEngineActionCode() : "Domain event";
			if ("SCHEDULED".equalsIgnoreCase(ep.getTriggerKind())) {
				return code + " (scheduled)";
			}
			return code;
		}
		return endpointDisplayName(ep, labelByRoute);
	}

	private static String trimOr(String value, String fallback) {
		if (value != null && !value.isBlank()) {
			return value.trim();
		}
		return fallback;
	}

	private static String trimOrNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private static String normalizeWorkflowType(String type) {
		if (type == null || type.isBlank()) {
			return "ACTION_PIPELINE";
		}
		return type.trim().toUpperCase();
	}
}
