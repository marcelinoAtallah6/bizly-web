package com.bizly.apigateway.workflow;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bizly.workflow")
public class BizlyWorkflowGatewayProperties {

	/**
	 * When true, the gateway may short-circuit mutating BM/PM/KYC/… calls into UM workflow capture (HTTP 202).
	 * Keep false until UM endpoint patterns and workflow configs are populated.
	 */
	private boolean deferralEnabled = false;

	public boolean isDeferralEnabled() {
		return deferralEnabled;
	}

	public void setDeferralEnabled(boolean deferralEnabled) {
		this.deferralEnabled = deferralEnabled;
	}
}
