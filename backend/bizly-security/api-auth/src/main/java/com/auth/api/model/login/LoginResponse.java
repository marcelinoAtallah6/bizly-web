package com.auth.api.model.login;

import java.util.List;

public class LoginResponse {

	public String token;
	public String refreshToken;
	public String sessionId;

	/** Role names assigned to the user (from DB). */
	public List<String> availableRoles;

	/**
	 * Active role for this session (mirrors {@code um_user_session.active_role_name}). Null means all
	 * assigned roles apply (no narrowing).
	 */
	public String activeRole;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public List<String> getAvailableRoles() {
		return availableRoles;
	}

	public void setAvailableRoles(List<String> availableRoles) {
		this.availableRoles = availableRoles;
	}

	public String getActiveRole() {
		return activeRole;
	}

	public void setActiveRole(String activeRole) {
		this.activeRole = activeRole;
	}

}
