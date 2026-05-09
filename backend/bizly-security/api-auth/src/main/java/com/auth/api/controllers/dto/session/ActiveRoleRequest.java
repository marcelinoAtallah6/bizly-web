package com.auth.api.controllers.dto.session;

public class ActiveRoleRequest {

	private String sessionId;

	/** Null or blank resets to “all assigned roles” (no single-role filter). */
	private String activeRoleName;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getActiveRoleName() {
		return activeRoleName;
	}

	public void setActiveRoleName(String activeRoleName) {
		this.activeRoleName = activeRoleName;
	}
}
