package com.auth.api.controllers.dto.registration;

public class AdminOnboardBusinessResponse {
	private Long businessId;
	private String businessName;
	private Long ownerUserId;
	private String ownerUsername;

	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
	public String getBusinessName() { return businessName; }
	public void setBusinessName(String businessName) { this.businessName = businessName; }
	public Long getOwnerUserId() { return ownerUserId; }
	public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
	public String getOwnerUsername() { return ownerUsername; }
	public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
}
