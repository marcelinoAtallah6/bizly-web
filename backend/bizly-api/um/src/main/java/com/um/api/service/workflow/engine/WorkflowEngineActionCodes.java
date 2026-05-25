package com.um.api.service.workflow.engine;

import java.util.Locale;
import java.util.Optional;

/**
 * Stable trigger keys for workflow definitions. Catalog lives in {@code UM_WORKFLOW_API_ENDPOINT}.
 * <ul>
 * <li>{@code EP:{id}} — canonical pipeline trigger (HTTP or DOMAIN row)</li>
 * <li>Legacy domain codes — resolved via {@code ENGINE_ACTION_CODE} on the same table</li>
 * <li>{@code SCR|{screenRoute}|{ACTION}} — approval-step targets only (menu permissions)</li>
 * </ul>
 */
public final class WorkflowEngineActionCodes {

	public static final String PREFIX_ENDPOINT = "EP:";
	public static final String PREFIX_SCREEN = "SCR|";

	private WorkflowEngineActionCodes() {
	}

	public static String forEndpoint(long endpointId) {
		return PREFIX_ENDPOINT + endpointId;
	}

	public static Optional<Long> parseEndpointId(String actionCode) {
		if (actionCode == null || actionCode.isBlank()) {
			return Optional.empty();
		}
		String c = actionCode.trim().toUpperCase(Locale.ROOT);
		if (!c.startsWith(PREFIX_ENDPOINT)) {
			return Optional.empty();
		}
		try {
			return Optional.of(Long.parseLong(c.substring(PREFIX_ENDPOINT.length())));
		} catch (NumberFormatException ex) {
			return Optional.empty();
		}
	}

	public static String forScreenAction(String screenRoute, String actionCode) {
		String route = screenRoute == null ? "" : screenRoute.trim();
		String action = actionCode == null ? "" : actionCode.trim().toUpperCase(Locale.ROOT);
		return PREFIX_SCREEN + route + "|" + action;
	}

	public static boolean isScreenActionCode(String actionCode) {
		return actionCode != null && actionCode.trim().toUpperCase(Locale.ROOT).startsWith(PREFIX_SCREEN);
	}
}
