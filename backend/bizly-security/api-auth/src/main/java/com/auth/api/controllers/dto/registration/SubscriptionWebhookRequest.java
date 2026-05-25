package com.auth.api.controllers.dto.registration;

/**
 * Payload from your payment provider webhook (map provider fields to these properties).
 */
public class SubscriptionWebhookRequest {
	private Long userId;
	private Long businessId;
	private String externalRef;
	private String status;

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }
	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
	public String getExternalRef() { return externalRef; }
	public void setExternalRef(String externalRef) { this.externalRef = externalRef; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
}
