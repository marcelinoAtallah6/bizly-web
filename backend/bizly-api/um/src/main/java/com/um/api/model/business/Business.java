package com.um.api.model.business;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.um.common.DatabaseConstants;

@Entity
@Table(name = "um_business", schema = DatabaseConstants.SCHEMA)
public class Business {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_name", nullable = false, length = 200)
	private String businessName;

	@Column(name = "business_type", length = 80)
	private String businessType;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Column(name = "created_by", nullable = false)
	private Long createdBy;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "onboarding_source", nullable = false, length = 30)
	private String onboardingSource = "PUBLIC";

	@Column(name = "business_type_role_id")
	private Long businessTypeRoleId;

	@Column(name = "subscription_status", length = 30)
	private String subscriptionStatus;

	@Column(name = "subscription_verified_at")
	private LocalDateTime subscriptionVerifiedAt;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getBusinessName() { return businessName; }
	public void setBusinessName(String businessName) { this.businessName = businessName; }
	public String getBusinessType() { return businessType; }
	public void setBusinessType(String businessType) { this.businessType = businessType; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public Long getCreatedBy() { return createdBy; }
	public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
	public String getOnboardingSource() { return onboardingSource; }
	public void setOnboardingSource(String onboardingSource) { this.onboardingSource = onboardingSource; }
	public Long getBusinessTypeRoleId() { return businessTypeRoleId; }
	public void setBusinessTypeRoleId(Long businessTypeRoleId) { this.businessTypeRoleId = businessTypeRoleId; }
	public String getSubscriptionStatus() { return subscriptionStatus; }
	public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }
	public LocalDateTime getSubscriptionVerifiedAt() { return subscriptionVerifiedAt; }
	public void setSubscriptionVerifiedAt(LocalDateTime subscriptionVerifiedAt) { this.subscriptionVerifiedAt = subscriptionVerifiedAt; }
}
