package com.auth.api.model.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "um_platform_config", schema = "um")
public class PlatformConfigEntity {

	@Id
	private Long id = 1L;

	@Column(name = "approval_flow_enabled", nullable = false)
	private Integer approvalFlowEnabled = 1;

	@Column(name = "subscription_flow_enabled", nullable = false)
	private Integer subscriptionFlowEnabled = 0;

	@Column(name = "public_registration_expiry_days", nullable = false)
	private Integer publicRegistrationExpiryDays = 30;

	public boolean isApprovalFlowEnabled() {
		return approvalFlowEnabled != null && approvalFlowEnabled == 1;
	}

	public boolean isSubscriptionFlowEnabled() {
		return subscriptionFlowEnabled != null && subscriptionFlowEnabled == 1;
	}

	public Integer getPublicRegistrationExpiryDays() { return publicRegistrationExpiryDays; }
}
