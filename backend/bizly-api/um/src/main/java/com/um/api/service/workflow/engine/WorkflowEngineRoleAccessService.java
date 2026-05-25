package com.um.api.service.workflow.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.um.api.model.workflow.engine.WorkflowDefinition;

/**
 * When {@code roleRestricted} is false or role list is empty, all users may run the pipeline.
 * When restricted, the caller's role codes must match (optionally including child roles).
 */
@Service
public class WorkflowEngineRoleAccessService {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ObjectMapper objectMapper;

	public boolean canRunPipeline(WorkflowDefinition def, String username) {
		if (def == null || !def.isRoleRestricted()) {
			return true;
		}
		List<String> allowed = parseRoleCodes(def.getRoleCodesJson());
		if (allowed.isEmpty()) {
			return true;
		}
		Set<String> userRoles = resolveUserRoleCodes(username, def.getBusinessId());
		if (userRoles.isEmpty()) {
			return false;
		}
		Set<String> effectiveAllowed = new HashSet<>(allowed);
		if (def.isAllowChildRoleInherit()) {
			effectiveAllowed.addAll(resolveDescendantRoleCodes(allowed, def.getBusinessId()));
		}
		for (String r : userRoles) {
			if (effectiveAllowed.contains(r)) {
				return true;
			}
		}
		return false;
	}

	private Set<String> resolveUserRoleCodes(String username, Long businessId) {
		String sql = "SELECT r.NAME FROM UM.UM_USER u "
				+ "JOIN UM.UM_USER_ROLE ur ON ur.USER_ID = u.ID "
				+ "JOIN UM.UM_ROLE r ON r.ID = ur.ROLE_ID "
				+ "WHERE LOWER(u.USERNAME) = LOWER(?) AND (u.BUSINESS_ID IS NULL OR u.BUSINESS_ID = ? OR ? IS NULL)";
		List<String> codes = jdbcTemplate.query(sql, (rs, i) -> rs.getString(1), username, businessId, businessId);
		return new HashSet<>(codes);
	}

	private Set<String> resolveDescendantRoleCodes(List<String> parentRoleCodes, Long businessId) {
		if (parentRoleCodes.isEmpty()) {
			return Collections.emptySet();
		}
		Set<String> out = new HashSet<>();
		Set<Long> frontier = new HashSet<>(loadRoleIds(parentRoleCodes, businessId));
		Set<Long> visited = new HashSet<>();
		while (!frontier.isEmpty()) {
			visited.addAll(frontier);
			List<Long> ids = new ArrayList<>(frontier);
			String in = String.join(",", Collections.nCopies(ids.size(), "?"));
			List<MapRow> children = jdbcTemplate.query(
					"SELECT ID, NAME FROM UM.UM_ROLE WHERE PARENT_ROLE_ID IN (" + in + ")",
					(rs, i) -> new MapRow(rs.getLong(1), rs.getString(2)),
					ids.toArray());
			frontier.clear();
			for (MapRow row : children) {
				if (row.code != null && !row.code.isBlank()) {
					out.add(row.code.trim());
				}
				if (!visited.contains(row.id)) {
					frontier.add(row.id);
				}
			}
		}
		return out;
	}

	private List<Long> loadRoleIds(List<String> roleCodes, Long businessId) {
		if (roleCodes.isEmpty()) {
			return List.of();
		}
		String in = String.join(",", Collections.nCopies(roleCodes.size(), "?"));
		List<Object> args = new ArrayList<>(roleCodes);
		args.add(businessId);
		args.add(businessId);
		return jdbcTemplate.query(
				"SELECT ID FROM UM.UM_ROLE WHERE NAME IN (" + in + ")"
						+ " AND (BUSINESS_ID IS NULL OR BUSINESS_ID = ? OR ? IS NULL)",
				(rs, i) -> rs.getLong(1),
				args.toArray());
	}

	private List<String> parseRoleCodes(String json) {
		if (json == null || json.isBlank()) {
			return List.of();
		}
		try {
			return objectMapper.readValue(json, new TypeReference<List<String>>() {
			});
		} catch (Exception ex) {
			return List.of();
		}
	}

	private static final class MapRow {
		final long id;
		final String code;

		MapRow(long id, String code) {
			this.id = id;
			this.code = code;
		}
	}
}
