package com.um.api.service.workflow.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.um.api.repository.user.UserRepository;

/**
 * Builds notification template context (email, name, username) for workflow engine triggers.
 */
@Component
public class WorkflowNotificationContextBuilder {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public Map<String, Object> enrich(Map<String, Object> context, Long businessId) {
		Map<String, Object> ctx = context != null ? new HashMap<>(context) : new HashMap<>();
		if (businessId != null) {
			ctx.putIfAbsent("businessId", businessId);
		}
		Long userId = longVal(ctx.get("userId"));
		if (userId != null) {
			userRepository.findById(userId).ifPresent(u -> {
				if (u.getEmail() != null && !u.getEmail().isBlank()) {
					ctx.putIfAbsent("email", u.getEmail().trim());
				}
				if (u.getUsername() != null && !u.getUsername().isBlank()) {
					ctx.putIfAbsent("username", u.getUsername().trim());
				}
				if (u.getFirstName() != null) {
					ctx.putIfAbsent("firstName", u.getFirstName());
				}
				if (u.getLastName() != null) {
					ctx.putIfAbsent("lastName", u.getLastName());
				}
			});
		}
		Long customerId = longVal(ctx.get("customerId"));
		if (customerId != null) {
			loadCustomerIntoContext(ctx, customerId);
		}
		String username = stringVal(ctx.get("username"));
		if (username != null && !username.isBlank()) {
			userRepository.findFirstByUsernameOrderByIdAsc(username.trim()).ifPresent(u -> {
				if (u.getId() != null) {
					ctx.putIfAbsent("userId", u.getId());
				}
				if (u.getEmail() != null && !u.getEmail().isBlank()) {
					ctx.putIfAbsent("email", u.getEmail().trim());
				}
				if (u.getFirstName() != null) {
					ctx.putIfAbsent("firstName", u.getFirstName());
				}
				if (u.getLastName() != null) {
					ctx.putIfAbsent("lastName", u.getLastName());
				}
			});
		}
		return ctx;
	}

	public Map<String, Object> forUsername(String username, Long businessId) {
		Map<String, Object> ctx = new HashMap<>();
		if (username != null && !username.isBlank()) {
			ctx.put("username", username.trim());
		}
		return enrich(ctx, businessId);
	}

	private void loadCustomerIntoContext(Map<String, Object> ctx, Long customerId) {
		try {
			List<Map<String, Object>> rows = jdbcTemplate.queryForList(
					"SELECT EMAIL, FIRST_NAME, LAST_NAME, FULL_NAME FROM UM.KYC_CUSTOMER WHERE ID = ?",
					customerId);
			if (rows.isEmpty()) {
				return;
			}
			Map<String, Object> row = rows.get(0);
			String email = stringVal(row.get("EMAIL"));
			if (email != null && !email.isBlank()) {
				ctx.putIfAbsent("email", email);
			}
			String fn = stringVal(row.get("FIRST_NAME"));
			String ln = stringVal(row.get("LAST_NAME"));
			String full = stringVal(row.get("FULL_NAME"));
			if (fn != null) {
				ctx.putIfAbsent("firstName", fn);
			}
			if (ln != null) {
				ctx.putIfAbsent("lastName", ln);
			}
			if (full != null && !full.isBlank()) {
				ctx.putIfAbsent("name", full);
			}
		} catch (Exception ignored) {
			// KYC schema optional in some environments
		}
	}

	private static String stringVal(Object o) {
		return o == null ? null : String.valueOf(o).trim();
	}

	private static Long longVal(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Number) {
			return ((Number) o).longValue();
		}
		try {
			return Long.parseLong(o.toString());
		} catch (NumberFormatException ex) {
			return null;
		}
	}
}
