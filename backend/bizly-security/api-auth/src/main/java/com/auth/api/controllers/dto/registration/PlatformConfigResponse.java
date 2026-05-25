package com.auth.api.controllers.dto.registration;

public class PlatformConfigResponse {
	private boolean approvalFlowEnabled;
	private boolean subscriptionFlowEnabled;
	private int publicRegistrationExpiryDays;

	public boolean isApprovalFlowEnabled() { return approvalFlowEnabled; }
	public void setApprovalFlowEnabled(boolean approvalFlowEnabled) { this.approvalFlowEnabled = approvalFlowEnabled; }
	public boolean isSubscriptionFlowEnabled() { return subscriptionFlowEnabled; }
	public void setSubscriptionFlowEnabled(boolean subscriptionFlowEnabled) { this.subscriptionFlowEnabled = subscriptionFlowEnabled; }
	public int getPublicRegistrationExpiryDays() { return publicRegistrationExpiryDays; }
	public void setPublicRegistrationExpiryDays(int publicRegistrationExpiryDays) { this.publicRegistrationExpiryDays = publicRegistrationExpiryDays; }
}
