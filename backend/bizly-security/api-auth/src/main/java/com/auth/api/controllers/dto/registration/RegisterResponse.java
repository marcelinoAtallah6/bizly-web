package com.auth.api.controllers.dto.registration;

import com.auth.api.model.login.LoginResponse;

/**
 * Result of a successful sign-up. The {@link #session} field is a full
 * {@link LoginResponse}; the SPA persists it like a normal login response and
 * lands the user on the dashboard with zero extra round-trips.
 */
public class RegisterResponse {

	private Long userId;
	private String username;
	private Long businessId;
	private String businessName;

	/** Auto-issued session — same shape as POST /auth/login. */
	private LoginResponse session;

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }

	public String getBusinessName() { return businessName; }
	public void setBusinessName(String businessName) { this.businessName = businessName; }

	public LoginResponse getSession() { return session; }
	public void setSession(LoginResponse session) { this.session = session; }
}
