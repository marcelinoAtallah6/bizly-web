package com.auth.api.controllers.dto.admin;

import java.time.LocalDateTime;

/** Lightweight projection returned by the admin business autocomplete. */
public class AdminBusinessDto {

	private Long id;
	private String businessName;
	private String businessType;
	private String status;
	private LocalDateTime createdAt;

	public AdminBusinessDto() {}

	public AdminBusinessDto(Long id, String businessName, String businessType, String status, LocalDateTime createdAt) {
		this.id = id;
		this.businessName = businessName;
		this.businessType = businessType;
		this.status = status;
		this.createdAt = createdAt;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getBusinessName() { return businessName; }
	public void setBusinessName(String businessName) { this.businessName = businessName; }
	public String getBusinessType() { return businessType; }
	public void setBusinessType(String businessType) { this.businessType = businessType; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
