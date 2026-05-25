package com.um.api.model.config;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.um.common.DatabaseConstants;

@Entity
@Table(name = "um_platform_config", schema = DatabaseConstants.SCHEMA)
public class PlatformConfig {

	@Id
	private Long id = 1L;

	@Column(name = "approval_flow_enabled", nullable = false)
	private Integer approvalFlowEnabled = 1;

	@Column(name = "subscription_flow_enabled", nullable = false)
	private Integer subscriptionFlowEnabled = 0;

	@Column(name = "public_registration_expiry_days", nullable = false)
	private Integer publicRegistrationExpiryDays = 30;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "updated_by")
	private Long updatedBy;

	public boolean isApprovalFlowEnabled() {
		return approvalFlowEnabled != null && approvalFlowEnabled == 1;
	}

	public boolean isSubscriptionFlowEnabled() {
		return subscriptionFlowEnabled != null && subscriptionFlowEnabled == 1;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Integer getApprovalFlowEnabled() { return approvalFlowEnabled; }
	public void setApprovalFlowEnabled(Integer v) { this.approvalFlowEnabled = v; }
	public Integer getSubscriptionFlowEnabled() { return subscriptionFlowEnabled; }
	public void setSubscriptionFlowEnabled(Integer v) { this.subscriptionFlowEnabled = v; }
	public Integer getPublicRegistrationExpiryDays() { return publicRegistrationExpiryDays; }
	public void setPublicRegistrationExpiryDays(Integer v) { this.publicRegistrationExpiryDays = v; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
	public Long getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
}
