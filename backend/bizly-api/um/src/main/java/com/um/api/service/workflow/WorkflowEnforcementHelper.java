package com.um.api.service.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.um.api.domain.RoleKind;
import com.um.api.model.role.Role;
import com.um.api.model.workflow.WorkflowConfig;
import com.um.api.repository.role.RoleRepository;
import com.um.api.repository.workflow.WorkflowConfigRepository;
import com.um.api.service.workflow.engine.WorkflowEngineApprovalRuntimeService;

/**
 * Shared workflow resolution and maker/checker role matching (including team-role ancestry).
 */
@Component
public class WorkflowEnforcementHelper {

	@Autowired
	private WorkflowConfigRepository configRepository;
	@Autowired
	private WorkflowEngineApprovalRuntimeService approvalRuntime;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Resolves the workflow that applies to a mutating API call.
	 * <p>
	 * A tenant-specific row with {@code hasWorkflow=false} disables workflow for that business even when a
	 * global row exists. With no tenant row, the global active configuration applies.
	 */
	public Optional<WorkflowConfig> resolveActiveWorkflow(String screenRoute, String actionCode, Long businessId) {
		String screen = screenRoute == null ? "" : screenRoute.trim();
		String action = actionCode == null ? "" : actionCode.trim();
		Optional<WorkflowConfig> fromEngine = approvalRuntime.materializeConfig(screen, action, businessId);
		if (fromEngine.isPresent()) {
			WorkflowConfig materialized = fromEngine.get();
			String builtInKey = materialized.getBuiltInKey();
			if (builtInKey != null && !builtInKey.isBlank()) {
				Optional<WorkflowConfig> persisted = configRepository.findFirstByBuiltInKey(builtInKey.trim());
				if (persisted.isPresent()) {
					return persisted.filter(WorkflowConfig::isHasWorkflow);
				}
			}
			return Optional.empty();
		}
		List<WorkflowConfig> rows = configRepository.findByScreenNameIgnoreCaseAndActionNameIgnoreCase(screen, action);
		if (businessId != null) {
			Optional<WorkflowConfig> tenant = rows.stream()
					.filter(c -> c.getBusinessId() != null && c.getBusinessId().equals(businessId))
					.findFirst();
			if (tenant.isPresent()) {
				return tenant.filter(WorkflowConfig::isHasWorkflow);
			}
		}
		return rows.stream().filter(c -> c.getBusinessId() == null && c.isHasWorkflow()).findFirst();
	}

	public boolean makerRestrictionsApply(WorkflowConfig cfg) {
		if (cfg == null || !cfg.isHasWorkflow()) {
			return false;
		}
		return !nonBlankEntries(firstTierEntries(parseTierMatrix(cfg.getMakerUsersJson()))).isEmpty()
				|| !nonBlankEntries(firstTierEntries(parseTierMatrix(cfg.getMakerRolesJson()))).isEmpty();
	}

	/**
	 * Whether a mutating request should be captured into the approval workflow.
	 * <p>
	 * When maker roles/users are configured, only those principals (and descendant team roles) are in
	 * scope; every other role bypasses workflow and the API runs immediately. When makers are not
	 * configured, an active workflow applies to all roles.
	 */
	public boolean shouldDeferMutatingRequest(WorkflowConfig cfg, List<String> roleNamesUpper, String username,
			Long businessId) {
		if (cfg == null || !cfg.isHasWorkflow()) {
			return false;
		}
		if (!makerRestrictionsApply(cfg)) {
			return true;
		}
		return isMakerSide(cfg, roleNamesUpper, username, businessId);
	}

	public boolean isMakerSide(WorkflowConfig cfg, List<String> roleNamesUpper, String username, Long businessId) {
		if (cfg == null || username == null || username.isBlank()) {
			return false;
		}
		String u = username.trim();
		for (String x : nonBlankEntries(firstTierEntries(parseTierMatrix(cfg.getMakerUsersJson())))) {
			if (u.equalsIgnoreCase(x)) {
				return true;
			}
		}
		List<String> configuredRoles = nonBlankEntries(firstTierEntries(parseTierMatrix(cfg.getMakerRolesJson())));
		if (configuredRoles.isEmpty()) {
			return false;
		}
		Set<String> effectiveRoles = expandRoleNamesWithAncestors(roleNamesUpper, businessId);
		for (String r : configuredRoles) {
			if (effectiveRoles.contains(r.toUpperCase(Locale.ROOT))) {
				return true;
			}
		}
		return false;
	}

	public boolean isCheckerForTier(WorkflowConfig cfg, List<String> roleNamesUpper, String username,
			Integer tier1Based, Long businessId) {
		if (cfg == null || tier1Based == null || tier1Based < 1) {
			return false;
		}
		List<List<String>> rt = parseTierMatrix(cfg.getCheckerRolesJson());
		List<List<String>> ut = parseTierMatrix(cfg.getCheckerUsersJson());
		int idx = tier1Based - 1;

		List<String> usersThis = nonBlankEntries(tierAt(ut, idx));
		String u = username == null ? "" : username.trim();
		if (!usersThis.isEmpty()) {
			if (u.isEmpty()) {
				return false;
			}
			for (String x : usersThis) {
				if (u.equalsIgnoreCase(x)) {
					return true;
				}
			}
			return false;
		}

		List<String> rolesThis = nonBlankEntries(tierAt(rt, idx));
		if (rolesThis.isEmpty()) {
			return false;
		}
		Set<String> effectiveRoles = expandRoleNamesWithAncestors(roleNamesUpper, businessId);
		for (String role : rolesThis) {
			if (effectiveRoles.contains(role.toUpperCase(Locale.ROOT))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Principal role names plus every ancestor team-role name (walk {@code parent_role_id} upward).
	 * Child roles therefore satisfy maker/checker lists that name a parent role.
	 */
	public Set<String> expandRoleNamesWithAncestors(List<String> principalRoleNamesUpper, Long businessId) {
		Set<String> expanded = new HashSet<>();
		if (principalRoleNamesUpper != null) {
			for (String r : principalRoleNamesUpper) {
				if (r != null && !r.isBlank()) {
					expanded.add(r.trim().toUpperCase(Locale.ROOT));
				}
			}
		}
		if (businessId == null || expanded.isEmpty()) {
			return expanded;
		}
		List<Role> teamRoles = roleRepository.findByBusinessIdAndRoleKind(businessId, RoleKind.BUSINESS_TEAM.name());
		Map<String, Role> byNameUpper = new HashMap<>();
		Map<Long, Role> byId = new HashMap<>();
		for (Role r : teamRoles) {
			byId.put(r.getId(), r);
			if (r.getName() != null && !r.getName().isBlank()) {
				byNameUpper.putIfAbsent(r.getName().trim().toUpperCase(Locale.ROOT), r);
			}
		}
		for (String pr : new ArrayList<>(expanded)) {
			Role start = byNameUpper.get(pr);
			if (start == null) {
				continue;
			}
			walkAncestors(start, byId, expanded);
		}
		return expanded;
	}

	private void walkAncestors(Role start, Map<Long, Role> teamById, Set<String> out) {
		Long walkId = start.getId();
		Set<Long> seen = new HashSet<>();
		while (walkId != null && seen.add(walkId)) {
			Role walk = teamById.get(walkId);
			if (walk == null) {
				walk = roleRepository.findById(walkId).orElse(null);
			}
			if (walk == null) {
				break;
			}
			if (walk.getName() != null && !walk.getName().isBlank()) {
				out.add(walk.getName().trim().toUpperCase(Locale.ROOT));
			}
			walkId = walk.getParentRoleId();
		}
	}

	public List<List<String>> parseTierMatrix(String json) {
		List<List<String>> tiers = new ArrayList<>();
		if (json == null || json.isBlank()) {
			return tiers;
		}
		try {
			JsonNode root = objectMapper.readTree(json);
			if (!root.isArray() || root.size() == 0) {
				return tiers;
			}
			if (root.get(0).isArray()) {
				for (JsonNode tier : root) {
					List<String> row = new ArrayList<>();
					if (tier.isArray()) {
						for (JsonNode n : tier) {
							if (n.isTextual()) {
								row.add(n.asText().trim());
							}
						}
					}
					tiers.add(row);
				}
			} else {
				List<String> one = new ArrayList<>();
				for (JsonNode n : root) {
					if (n.isTextual()) {
						one.add(n.asText().trim());
					}
				}
				if (!one.isEmpty()) {
					tiers.add(one);
				}
			}
		} catch (Exception ignored) {
			return List.of();
		}
		return tiers;
	}

	private static List<String> firstTierEntries(List<List<String>> tiers) {
		if (tiers == null || tiers.isEmpty()) {
			return List.of();
		}
		List<String> row = tiers.get(0);
		return row == null ? List.of() : row;
	}

	private static List<String> tierAt(List<List<String>> tiers, int idx) {
		if (tiers == null || tiers.isEmpty()) {
			return List.of();
		}
		List<String> row = idx < tiers.size() ? tiers.get(idx) : List.of();
		if ((row == null || row.isEmpty()) && tiers.size() == 1) {
			row = tiers.get(0);
		}
		return row == null ? List.of() : row;
	}

	private static List<String> nonBlankEntries(List<String> in) {
		if (in == null || in.isEmpty()) {
			return List.of();
		}
		return in.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).collect(Collectors.toList());
	}
}
