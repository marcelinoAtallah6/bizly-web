package com.auth.api.service.registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth.api.model.config.PlatformConfigEntity;
import com.auth.api.repository.config.PlatformConfigRepository;

@Service
public class RegistrationPolicyService {

	@Autowired
	private PlatformConfigRepository platformConfigRepository;
	@Autowired
	private RegistrationWorkflowResolver registrationWorkflowResolver;

	@Value("${app.registration.require-super-admin-business-approval:true}")
	private boolean legacyRequireApproval;

	public boolean isApprovalFlowEnabled() {
		return platformConfigRepository.findById(1L)
				.map(PlatformConfigEntity::isApprovalFlowEnabled)
				.orElse(legacyRequireApproval);
	}

	public boolean isSubscriptionFlowEnabled() {
		return platformConfigRepository.findById(1L)
				.map(PlatformConfigEntity::isSubscriptionFlowEnabled)
				.orElse(false);
	}

	public int getPublicRegistrationExpiryDays() {
		return platformConfigRepository.findById(1L)
				.map(PlatformConfigEntity::getPublicRegistrationExpiryDays)
				.filter(d -> d != null && d > 0)
				.orElse(30);
	}

	/**
	 * Public sign-up uses {@code PENDING_APPROVAL} only when platform policy allows it and a workflow
	 * config row has {@code HAS_WORKFLOW = 1} (engine-synced or legacy built-in).
	 */
	public boolean requiresApprovalForPublicRegistration() {
		return isApprovalFlowEnabled() && registrationWorkflowResolver.isRegistrationApprovalActive();
	}
}
