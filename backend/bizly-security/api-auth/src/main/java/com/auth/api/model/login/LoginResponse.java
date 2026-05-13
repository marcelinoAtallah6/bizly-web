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

	/** True until the user finishes the welcome wizard. Drives the post-login redirect on the client. */
	public Boolean firstLogin;

	/** Tenant id the user currently belongs to. NULL until business registration is complete. */
	public Long businessId;

	/** Display name of the business; convenience for the client. */
	public String businessName;

	/** Code of the role level for the user's currently active role (ADMIN / BUSINESS). */
	public String roleLevel;

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

	public Boolean getFirstLogin() { return firstLogin; }
	public void setFirstLogin(Boolean firstLogin) { this.firstLogin = firstLogin; }

	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }

	public String getBusinessName() { return businessName; }
	public void setBusinessName(String businessName) { this.businessName = businessName; }

	public String getRoleLevel() { return roleLevel; }
	public void setRoleLevel(String roleLevel) { this.roleLevel = roleLevel; }
}
