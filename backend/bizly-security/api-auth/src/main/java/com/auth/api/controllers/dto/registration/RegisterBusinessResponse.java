package com.auth.api.controllers.dto.registration;

import com.auth.api.model.login.LoginResponse;

/**
 * Wraps a freshly-issued {@link LoginResponse} so the client immediately
 * receives a JWT that includes the new {@code businessId} claim. The client
 * must overwrite its stored tokens with the values in {@code session}.
 */
public class RegisterBusinessResponse {

	private Long businessId;
	private String businessName;
	private LoginResponse session;

	/**
	 * When true, the tenant is not fully active yet (e.g. business status is not {@code ACTIVE}) and
	 * the SPA should start the business-registration approval workflow. Normal self-serve registration
	 * uses {@code ACTIVE} businesses, so this stays false for the default path.
	 */
	private boolean requiresBusinessApprovalWorkflow;

	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }

	public String getBusinessName() { return businessName; }
	public void setBusinessName(String businessName) { this.businessName = businessName; }

	public LoginResponse getSession() { return session; }
	public void setSession(LoginResponse session) { this.session = session; }

	public boolean isRequiresBusinessApprovalWorkflow() {
		return requiresBusinessApprovalWorkflow;
	}

	public void setRequiresBusinessApprovalWorkflow(boolean requiresBusinessApprovalWorkflow) {
		this.requiresBusinessApprovalWorkflow = requiresBusinessApprovalWorkflow;
	}
}
