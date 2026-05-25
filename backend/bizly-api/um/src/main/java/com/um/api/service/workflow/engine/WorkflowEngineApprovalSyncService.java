package com.um.api.service.workflow.engine;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.um.api.model.workflow.WorkflowApiEndpoint;
import com.um.api.model.workflow.WorkflowConfig;
import com.um.api.model.workflow.engine.WorkflowDefinition;
import com.um.api.model.workflow.engine.WorkflowStep;
import com.um.api.repository.workflow.WorkflowApiEndpointRepository;
import com.um.api.repository.workflow.WorkflowConfigRepository;
import com.um.api.repository.workflow.engine.WorkflowDefinitionRepository;
import com.um.api.repository.workflow.engine.WorkflowStepRepository;
import com.um.api.service.workflow.WorkflowEnforcementHelper;
import com.um.exception.ServiceException;

/**
 * Publishes APPROVAL steps from the workflow engine into {@code UM_WORKFLOW_CONFIG} so the API
 * gateway maker–checker path uses the same pipeline the user designed on the canvas.
 */
@Service
public class WorkflowEngineApprovalSyncService {

	public static final String BUILT_IN_PREFIX = "WF_ENGINE:DEF:";

	@Autowired
	private WorkflowConfigRepository configRepository;
	@Autowired
	private WorkflowApiEndpointRepository endpointRepository;
	@Autowired
	private WorkflowStepRepository stepRepository;
	@Autowired
	private WorkflowDefinitionRepository definitionRepository;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private WorkflowEnforcementHelper workflowEnforcement;

	/**
	 * Upserts gateway approval rules for each active APPROVAL step and links {@code workflow_config_id}
	 * on the step row.
	 */
	public void syncOnPublish(WorkflowDefinition definition, List<WorkflowStep> steps, String username) {
		Set<Long> activeConfigIds = new HashSet<>();
		for (WorkflowStep step : steps) {
			if (!WorkflowEngineOrchestratorService.STEP_APPROVAL.equalsIgnoreCase(step.getStepType())
					|| !step.isActive()) {
				continue;
			}
			ApprovalStepParsed approval = parseApprovalStep(step.getConfigJson());
			validateApproval(approval, step);
			WorkflowConfig cfg = upsertConfig(definition, step, approval, username);
			step.setWorkflowConfigId(cfg.getId());
			step.setConfigJson(mergeConfigWithConfigId(step.getConfigJson(), cfg.getId()));
			stepRepository.save(step);
			activeConfigIds.add(cfg.getId());
		}
		deactivateOrphanedConfigs(definition.getId(), activeConfigIds);
	}

	public void deactivateForDefinition(Long definitionId) {
		if (definitionId == null) {
			return;
		}
		deactivateConfigsWithBuiltInPrefix(definitionId);
		deactivateConfigsForApprovalSteps(definitionId);
	}

	/**
	 * Disables all gateway configs produced by this pipeline (by built-in key and by screen/action).
	 */
	public void deactivateAllApprovalConfigsForDefinition(Long definitionId) {
		if (definitionId == null) {
			return;
		}
		deactivateConfigsWithBuiltInPrefix(definitionId);
		deactivateConfigsForApprovalSteps(definitionId);
	}

	private void deactivateConfigsWithBuiltInPrefix(Long definitionId) {
		for (WorkflowConfig cfg : configRepository.findByBuiltInKeyStartingWith(builtInPrefix(definitionId))) {
			if (cfg.isHasWorkflow()) {
				cfg.setHasWorkflow(false);
				configRepository.save(cfg);
			}
		}
	}

	private void deactivateConfigsForApprovalSteps(Long definitionId) {
		Long businessId = definitionRepository.findById(definitionId).map(WorkflowDefinition::getBusinessId).orElse(null);
		List<WorkflowStep> steps = stepRepository.findByDefinitionIdOrderByStepOrderAsc(definitionId);
		for (WorkflowStep step : steps) {
			if (!WorkflowEngineOrchestratorService.STEP_APPROVAL.equalsIgnoreCase(step.getStepType())) {
				continue;
			}
			ApprovalStepParsed approval = parseApprovalStep(step.getConfigJson());
			if (approval.screenRoute.isBlank() || approval.actionName.isBlank()) {
				continue;
			}
			configRepository
					.findByScreenNameIgnoreCaseAndActionNameIgnoreCase(approval.screenRoute, approval.actionName)
					.stream()
					.filter(c -> Objects.equals(c.getBusinessId(), businessId))
					.forEach(cfg -> {
						if (cfg.isHasWorkflow()) {
							cfg.setHasWorkflow(false);
							configRepository.save(cfg);
						}
					});
		}
	}

	private WorkflowConfig upsertConfig(WorkflowDefinition definition, WorkflowStep step,
			ApprovalStepParsed approval, String username) {
		String builtInKey = builtInKey(definition.getId(), step.getId());
		WorkflowConfig cfg = configRepository.findFirstByBuiltInKey(builtInKey).orElse(null);
		if (cfg == null) {
			cfg = configRepository
					.findByScreenNameIgnoreCaseAndActionNameIgnoreCase(approval.screenRoute, approval.actionName)
					.stream()
					.filter(c -> Objects.equals(c.getBusinessId(), definition.getBusinessId()))
					.findFirst()
					.orElse(null);
		}
		if (cfg == null) {
			cfg = new WorkflowConfig();
		}
		cfg.setBuiltInKey(builtInKey);
		cfg.setScreenName(approval.screenRoute);
		cfg.setActionName(approval.actionName);
		cfg.setBusinessId(definition.getBusinessId());
		cfg.setHasWorkflow(true);

		boolean override = approval.userOverride;
		cfg.setMakerRolesJson(
				!override && !approval.makerRoles.isEmpty() ? toJsonFlatArray(approval.makerRoles) : null);
		cfg.setMakerUsersJson(
				override && !approval.makerUsers.isEmpty() ? toJsonFlatArray(approval.makerUsers) : null);
		cfg.setCheckerRolesJson(override ? null : serializeCheckerTiers(approval.checkerRoleTiers));
		cfg.setCheckerUsersJson(override ? serializeCheckerTiers(approval.checkerUserTiers) : null);

		applyLevelCount(cfg);
		assertCheckerPresent(cfg);
		if (cfg.getCreatedBy() == null || cfg.getCreatedBy().isBlank()) {
			cfg.setCreatedBy(username);
		}
		if (cfg.getCreatedAt() == null) {
			cfg.setCreatedAt(LocalDateTime.now());
		}
		return configRepository.save(cfg);
	}

	private void deactivateOrphanedConfigs(Long definitionId, Set<Long> keepConfigIds) {
		for (WorkflowConfig cfg : configRepository.findByBuiltInKeyStartingWith(builtInPrefix(definitionId))) {
			if (!keepConfigIds.contains(cfg.getId()) && cfg.isHasWorkflow()) {
				cfg.setHasWorkflow(false);
				configRepository.save(cfg);
			}
		}
	}

	private void validateApproval(ApprovalStepParsed approval, WorkflowStep step) {
		if (approval.screenRoute.isBlank() || approval.actionName.isBlank()) {
			throw new ServiceException(
					"Approval step " + stepLabel(step) + " requires a gateway API endpoint (screen + action).",
					HttpStatus.BAD_REQUEST);
		}
		if (approval.endpointId != null) {
			WorkflowApiEndpoint ep = endpointRepository.findById(approval.endpointId).orElse(null);
			if (ep == null || !ep.isActive() || ep.getPathAntPattern() == null || ep.getPathAntPattern().isBlank()) {
				throw new ServiceException(
						"Approval step " + stepLabel(step) + " references an invalid or inactive API endpoint.",
						HttpStatus.BAD_REQUEST);
			}
		}
		if (!hasCheckerAssignment(approval)) {
			throw new ServiceException(
					"Approval step " + stepLabel(step) + " requires at least one checker on level 1.",
					HttpStatus.BAD_REQUEST);
		}
	}

	private static boolean hasCheckerAssignment(ApprovalStepParsed approval) {
		List<List<String>> effective = approval.userOverride
				? stripTrailingEmptyTiers(approval.checkerUserTiers)
				: stripTrailingEmptyTiers(approval.checkerRoleTiers);
		return effective.stream().anyMatch(t -> !t.isEmpty());
	}

	private static String stepLabel(WorkflowStep step) {
		if (step.getDisplayLabel() != null && !step.getDisplayLabel().isBlank()) {
			return "\"" + step.getDisplayLabel().trim() + "\"";
		}
		return "#" + step.getStepOrder();
	}

	private ApprovalStepParsed parseApprovalStep(String configJson) {
		ApprovalStepParsed out = new ApprovalStepParsed();
		if (configJson == null || configJson.isBlank()) {
			out.checkerRoleTiers = List.of(List.of());
			out.checkerUserTiers = List.of(List.of());
			return out;
		}
		try {
			JsonNode root = objectMapper.readTree(configJson);
			out.screenRoute = text(root, "screenRoute");
			out.actionName = text(root, "actionName").toUpperCase(Locale.ROOT);
			out.userOverride = root.path("userOverride").asBoolean(false);
			out.makerRoles = stringList(root.get("makerRoles"));
			out.makerUsers = stringList(root.get("makerUsers"));
			out.checkerRoleTiers = parseTierListFromConfig(root.get("checkerRoleTiers"));
			out.checkerUserTiers = parseTierListFromConfig(root.get("checkerUserTiers"));
			if (out.checkerRoleTiers.isEmpty()) {
				List<String> legacy = stringList(root.get("roleNames"));
				if (!legacy.isEmpty()) {
					out.checkerRoleTiers = List.of(legacy);
				}
			}
			if (out.checkerRoleTiers.isEmpty()) {
				out.checkerRoleTiers = List.of(List.of());
			}
			if (out.checkerUserTiers.isEmpty()) {
				out.checkerUserTiers = out.checkerRoleTiers.stream().map(t -> List.<String>of())
						.collect(Collectors.toList());
			}
		} catch (ServiceException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ServiceException("Invalid approval step configuration.", HttpStatus.BAD_REQUEST);
		}
		return out;
	}

	private List<List<String>> parseTierListFromConfig(JsonNode node) {
		if (node == null || node.isNull() || !node.isArray()) {
			return List.of();
		}
		List<List<String>> tiers = new ArrayList<>();
		for (JsonNode tier : node) {
			if (tier.isArray()) {
				tiers.add(stringList(tier));
			}
		}
		return tiers;
	}

	private String mergeConfigWithConfigId(String configJson, Long configId) {
		try {
			ObjectNode root = configJson == null || configJson.isBlank()
					? objectMapper.createObjectNode()
					: (ObjectNode) objectMapper.readTree(configJson);
			root.put("workflowConfigId", configId);
			return objectMapper.writeValueAsString(root);
		} catch (Exception ex) {
			throw new ServiceException("Could not update approval step metadata.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String toJsonFlatArray(List<String> values) {
		try {
			ArrayNode arr = objectMapper.createArrayNode();
			for (String v : values) {
				if (v != null && !v.isBlank()) {
					arr.add(v.trim());
				}
			}
			return arr.isEmpty() ? null : objectMapper.writeValueAsString(arr);
		} catch (Exception ex) {
			throw new ServiceException("Could not serialise role list.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private String serializeCheckerTiers(List<List<String>> tiers) {
		List<List<String>> cleaned = stripTrailingEmptyTiers(tiers);
		List<List<String>> nonEmpty = cleaned.stream().filter(t -> !t.isEmpty()).collect(Collectors.toList());
		if (nonEmpty.isEmpty()) {
			return null;
		}
		try {
			if (nonEmpty.size() == 1) {
				return toJsonFlatArray(nonEmpty.get(0));
			}
			ArrayNode outer = objectMapper.createArrayNode();
			for (List<String> tier : nonEmpty) {
				ArrayNode inner = objectMapper.createArrayNode();
				for (String v : tier) {
					if (v != null && !v.isBlank()) {
						inner.add(v.trim());
					}
				}
				outer.add(inner);
			}
			return objectMapper.writeValueAsString(outer);
		} catch (Exception ex) {
			throw new ServiceException("Could not serialise checker tiers.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private static List<List<String>> stripTrailingEmptyTiers(List<List<String>> tiers) {
		if (tiers == null || tiers.isEmpty()) {
			return List.of(List.of());
		}
		List<List<String>> t = tiers.stream()
				.map(row -> row == null ? List.<String>of()
						: row.stream().filter(s -> s != null && !s.isBlank()).map(String::trim)
								.collect(Collectors.toList()))
				.collect(Collectors.toCollection(ArrayList::new));
		while (t.size() > 1 && t.get(t.size() - 1).isEmpty()) {
			t.remove(t.size() - 1);
		}
		return t;
	}

	private void applyLevelCount(WorkflowConfig cfg) {
		List<List<String>> rt = workflowEnforcement.parseTierMatrix(cfg.getCheckerRolesJson());
		List<List<String>> ut = workflowEnforcement.parseTierMatrix(cfg.getCheckerUsersJson());
		int n = Math.max(rt.size(), ut.size());
		if (n < 1) {
			n = 1;
		}
		if (n > 10) {
			throw new ServiceException("At most 10 checker levels are supported.", HttpStatus.BAD_REQUEST);
		}
		cfg.setLevelCount(n);
	}

	private static String builtInKey(Long definitionId, Long stepId) {
		return BUILT_IN_PREFIX + definitionId + ":STEP:" + stepId;
	}

	private static String builtInPrefix(Long definitionId) {
		return BUILT_IN_PREFIX + definitionId + ":";
	}

	private static String text(JsonNode node, String field) {
		if (node == null || !node.has(field)) {
			return "";
		}
		return node.get(field).asText("").trim();
	}

	private static List<String> stringList(JsonNode node) {
		if (node == null || !node.isArray()) {
			return List.of();
		}
		List<String> out = new ArrayList<>();
		for (JsonNode n : node) {
			if (n.isTextual() && !n.asText().isBlank()) {
				out.add(n.asText().trim());
			}
		}
		return out;
	}

	private void assertCheckerPresent(WorkflowConfig cfg) {
		List<List<String>> rt = workflowEnforcement.parseTierMatrix(cfg.getCheckerRolesJson());
		List<List<String>> ut = workflowEnforcement.parseTierMatrix(cfg.getCheckerUsersJson());
		int tiers = Math.max(Math.max(1, rt.size()), ut.size());
		for (int i = 0; i < tiers; i++) {
			List<String> r = i < rt.size() ? rt.get(i) : List.of();
			List<String> u = i < ut.size() ? ut.get(i) : List.of();
			if (!r.isEmpty() || !u.isEmpty()) {
				return;
			}
		}
		throw new ServiceException("Each approval step must define at least one checker.",
				HttpStatus.BAD_REQUEST);
	}

	private void resolveScreenActionFromEndpoint(ApprovalStepParsed approval) {
		if (approval.endpointId == null) {
			return;
		}
		WorkflowApiEndpoint ep = endpointRepository.findById(approval.endpointId)
				.orElseThrow(() -> new ServiceException("Unknown workflow API endpoint in approval step.",
						HttpStatus.BAD_REQUEST));
		if (ep.getScreenRoute() != null && !ep.getScreenRoute().isBlank()) {
			approval.screenRoute = ep.getScreenRoute().trim();
		}
		if (ep.getActionCode() != null && !ep.getActionCode().isBlank()) {
			approval.actionName = ep.getActionCode().trim().toUpperCase(Locale.ROOT);
		}
	}

	private static final class ApprovalStepParsed {
		Long endpointId;
		String screenRoute = "";
		String actionName = "";
		boolean userOverride;
		List<String> makerRoles = List.of();
		List<String> makerUsers = List.of();
		List<List<String>> checkerRoleTiers = List.of();
		List<List<String>> checkerUserTiers = List.of();
	}
}
