package com.um.api.service.workflow.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.um.api.model.workflow.WorkflowConfig;
import com.um.api.model.workflow.engine.WorkflowDefinition;
import com.um.api.model.workflow.engine.WorkflowStep;
import com.um.api.repository.workflow.engine.WorkflowDefinitionRepository;
import com.um.api.repository.workflow.engine.WorkflowStepRepository;

/**
 * Resolves maker–checker rules from published workflow engine pipelines (approval steps),
 * materializing a {@link WorkflowConfig} for the gateway and approval queue without requiring
 * a separate manual config row (sync on publish remains a cache for instance FKs).
 */
@Service
public class WorkflowEngineApprovalRuntimeService {

	public static final String BUILT_IN_PREFIX = WorkflowEngineApprovalSyncService.BUILT_IN_PREFIX;

	@Autowired
	private WorkflowDefinitionRepository definitionRepository;
	@Autowired
	private WorkflowStepRepository stepRepository;
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Active approval rule for a mutating screen + action, from a published pipeline.
	 */
	public Optional<WorkflowConfig> materializeConfig(String screenRoute, String actionCode, Long businessId) {
		return findMatchingApprovalStep(screenRoute, actionCode, businessId).map(this::toMaterializedConfig);
	}

	public Optional<ResolvedApprovalStep> findMatchingApprovalStep(String screenRoute, String actionCode,
			Long businessId) {
		String screen = screenRoute == null ? "" : screenRoute.trim();
		String action = actionCode == null ? "" : actionCode.trim().toUpperCase(Locale.ROOT);
		if (screen.isEmpty() || action.isEmpty()) {
			return Optional.empty();
		}
		List<WorkflowDefinition> defs = definitionRepository.findAllPublishedForBusinessScope(businessId);
		for (WorkflowDefinition def : defs) {
			List<WorkflowStep> steps = stepRepository.findByDefinitionIdAndIsActiveOrderByStepOrderAsc(def.getId(), 1);
			for (WorkflowStep step : steps) {
				if (!WorkflowEngineOrchestratorService.STEP_APPROVAL.equalsIgnoreCase(step.getStepType())) {
					continue;
				}
				ApprovalStepParsed parsed = parseApprovalStep(step.getConfigJson());
				if (screen.equalsIgnoreCase(parsed.screenRoute) && action.equalsIgnoreCase(parsed.actionName)) {
					return Optional.of(new ResolvedApprovalStep(def, step, parsed));
				}
			}
		}
		return Optional.empty();
	}

	public Optional<Long> parseDefinitionIdFromBuiltInKey(String builtInKey) {
		if (builtInKey == null || !builtInKey.startsWith(BUILT_IN_PREFIX)) {
			return Optional.empty();
		}
		String rest = builtInKey.substring(BUILT_IN_PREFIX.length());
		int colon = rest.indexOf(":STEP:");
		if (colon < 1) {
			return Optional.empty();
		}
		try {
			return Optional.of(Long.parseLong(rest.substring(0, colon)));
		} catch (NumberFormatException ex) {
			return Optional.empty();
		}
	}

	private WorkflowConfig toMaterializedConfig(ResolvedApprovalStep resolved) {
		WorkflowDefinition def = resolved.getDefinition();
		WorkflowStep step = resolved.getStep();
		ApprovalStepParsed approval = resolved.getParsed();

		WorkflowConfig cfg = new WorkflowConfig();
		cfg.setBuiltInKey(BUILT_IN_PREFIX + def.getId() + ":STEP:" + step.getId());
		cfg.setScreenName(approval.screenRoute);
		cfg.setActionName(approval.actionName);
		cfg.setBusinessId(def.getBusinessId());
		cfg.setHasWorkflow(true);

		boolean override = approval.userOverride;
		cfg.setMakerRolesJson(
				!override && !approval.makerRoles.isEmpty() ? toJsonFlat(approval.makerRoles) : null);
		cfg.setMakerUsersJson(
				override && !approval.makerUsers.isEmpty() ? toJsonFlat(approval.makerUsers) : null);
		cfg.setCheckerRolesJson(override ? null : serializeCheckerTiers(approval.checkerRoleTiers));
		cfg.setCheckerUsersJson(override ? serializeCheckerTiers(approval.checkerUserTiers) : null);

		int roleTiers = approval.checkerRoleTiers == null ? 0 : approval.checkerRoleTiers.size();
		int userTiers = approval.checkerUserTiers == null ? 0 : approval.checkerUserTiers.size();
		cfg.setLevelCount(Math.min(10, Math.max(1, Math.max(roleTiers, userTiers))));
		return cfg;
	}

	private ApprovalStepParsed parseApprovalStep(String configJson) {
		ApprovalStepParsed out = new ApprovalStepParsed();
		if (configJson == null || configJson.trim().isEmpty()) {
			return out;
		}
		try {
			JsonNode root = objectMapper.readTree(configJson);
			out.screenRoute = text(root, "screenRoute");
			out.actionName = text(root, "actionName").toUpperCase(Locale.ROOT);
			out.userOverride = root.path("userOverride").asBoolean(false);
			out.makerRoles = stringList(root.get("makerRoles"));
			out.makerUsers = stringList(root.get("makerUsers"));
			out.checkerRoleTiers = parseTierList(root.get("checkerRoleTiers"));
			out.checkerUserTiers = parseTierList(root.get("checkerUserTiers"));
			if (out.checkerRoleTiers.isEmpty()) {
				List<String> legacy = stringList(root.get("roleNames"));
				if (!legacy.isEmpty()) {
					out.checkerRoleTiers = Collections.singletonList(legacy);
				}
			}
		} catch (Exception ignored) {
			// keep defaults
		}
		return out;
	}

	private String toJsonFlat(List<String> values) {
		try {
			return objectMapper.writeValueAsString(values);
		} catch (Exception ex) {
			return "[]";
		}
	}

	private String serializeCheckerTiers(List<List<String>> tiers) {
		if (tiers == null || tiers.isEmpty()) {
			return null;
		}
		List<List<String>> nonEmpty = new ArrayList<>();
		for (List<String> tier : tiers) {
			if (tier != null && !tier.isEmpty()) {
				nonEmpty.add(tier);
			}
		}
		if (nonEmpty.isEmpty()) {
			return null;
		}
		if (nonEmpty.size() == 1) {
			return toJsonFlat(nonEmpty.get(0));
		}
		try {
			return objectMapper.writeValueAsString(nonEmpty);
		} catch (Exception ex) {
			return null;
		}
	}

	private static List<List<String>> parseTierList(JsonNode node) {
		if (node == null || !node.isArray()) {
			return Collections.emptyList();
		}
		return StreamSupport.stream(node.spliterator(), false)
				.filter(JsonNode::isArray)
				.map(WorkflowEngineApprovalRuntimeService::stringList)
				.filter(t -> !t.isEmpty())
				.collect(Collectors.toList());
	}

	private static List<String> stringList(JsonNode node) {
		if (node == null || !node.isArray()) {
			return Collections.emptyList();
		}
		return StreamSupport.stream(node.spliterator(), false)
				.filter(JsonNode::isTextual)
				.map(n -> n.asText().trim())
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());
	}

	private static String text(JsonNode node, String field) {
		if (node == null || !node.has(field)) {
			return "";
		}
		return node.get(field).asText("").trim();
	}

	/** Java 11–compatible holder (no records). */
	public static final class ResolvedApprovalStep {
		private final WorkflowDefinition definition;
		private final WorkflowStep step;
		private final ApprovalStepParsed parsed;

		public ResolvedApprovalStep(WorkflowDefinition definition, WorkflowStep step, ApprovalStepParsed parsed) {
			this.definition = definition;
			this.step = step;
			this.parsed = parsed;
		}

		public WorkflowDefinition getDefinition() {
			return definition;
		}

		public WorkflowStep getStep() {
			return step;
		}

		public ApprovalStepParsed getParsed() {
			return parsed;
		}
	}

	private static final class ApprovalStepParsed {
		String screenRoute = "";
		String actionName = "";
		boolean userOverride;
		List<String> makerRoles = Collections.emptyList();
		List<String> makerUsers = Collections.emptyList();
		List<List<String>> checkerRoleTiers = Collections.emptyList();
		List<List<String>> checkerUserTiers = Collections.emptyList();
	}
}
