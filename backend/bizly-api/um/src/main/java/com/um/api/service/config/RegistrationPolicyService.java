package com.um.api.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.um.api.model.config.PlatformConfig;
import com.um.api.repository.config.PlatformConfigRepository;

/**
 * Resolves whether public registration requires approval or can bypass via subscription.
 */
@Service
public class RegistrationPolicyService {

	@Autowired
	private PlatformConfigRepository platformConfigRepository;

	@Value("${app.registration.require-super-admin-business-approval:true}")
	private boolean legacyRequireApproval;

	public PlatformConfig load() {
		return platformConfigRepository.findById(1L).orElseGet(this::defaultConfig);
	}

	public boolean isApprovalFlowEnabled() {
		PlatformConfig cfg = load();
		if (cfg.getApprovalFlowEnabled() != null) {
			return cfg.isApprovalFlowEnabled();
		}
		return legacyRequireApproval;
	}

	public boolean isSubscriptionFlowEnabled() {
		return load().isSubscriptionFlowEnabled();
	}

	public int getPublicRegistrationExpiryDays() {
		Integer days = load().getPublicRegistrationExpiryDays();
		return days != null && days > 0 ? days : 30;
	}

	private PlatformConfig defaultConfig() {
		PlatformConfig c = new PlatformConfig();
		c.setId(1L);
		c.setApprovalFlowEnabled(legacyRequireApproval ? 1 : 0);
		c.setSubscriptionFlowEnabled(0);
		c.setPublicRegistrationExpiryDays(30);
		return c;
	}
}
