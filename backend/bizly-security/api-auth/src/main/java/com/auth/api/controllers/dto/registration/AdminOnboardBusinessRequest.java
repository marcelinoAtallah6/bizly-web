package com.auth.api.controllers.dto.registration;

public class AdminOnboardBusinessRequest {
	private String businessName;
	private Long businessTypeRoleId;
	private String email;
	private String firstName;
	private String lastName;

	public String getBusinessName() { return businessName; }
	public void setBusinessName(String businessName) { this.businessName = businessName; }
	public Long getBusinessTypeRoleId() { return businessTypeRoleId; }
	public void setBusinessTypeRoleId(Long businessTypeRoleId) { this.businessTypeRoleId = businessTypeRoleId; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName; }
	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
}
