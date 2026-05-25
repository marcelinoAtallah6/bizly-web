package com.auth.api.service.registration;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.api.controllers.dto.registration.SubscriptionWebhookRequest;
import com.auth.api.model.business.BusinessEntity;
import com.auth.api.model.subscription.UserSubscriptionEntity;
import com.auth.api.model.user.UserEntity;
import com.auth.api.repository.business.BusinessRepository;
import com.auth.api.repository.subscription.UserSubscriptionRepository;
import com.auth.api.repository.user.UserRepository;
import com.auth.common.ApiMessages;
import com.auth.config.Exception.ServiceException;

/**
 * Activates a publicly registered business when the payment provider confirms subscription payment.
 * Configure your provider to POST to {@code /auth/webhooks/payment/subscription}.
 */
@Service
public class SubscriptionActivationService {

	private static final Logger log = LoggerFactory.getLogger(SubscriptionActivationService.class);

	@Autowired private UserRepository userRepository;
	@Autowired private BusinessRepository businessRepository;
	@Autowired private UserSubscriptionRepository subscriptionRepository;
	@Autowired private RegistrationPolicyService registrationPolicyService;

	@Transactional
	public void handlePaymentWebhook(SubscriptionWebhookRequest req) {
		if (!registrationPolicyService.isSubscriptionFlowEnabled()) {
			throw new ServiceException("Subscription flow is disabled.", HttpStatus.NOT_IMPLEMENTED);
		}
		if (req == null || req.getExternalRef() == null || req.getExternalRef().isBlank()) {
			throw new ServiceException("externalRef is required", HttpStatus.BAD_REQUEST);
		}
		if (!"PAID".equalsIgnoreCase(req.getStatus())) {
			log.info("[SUBSCRIPTION_WEBHOOK] ignored status={}", req.getStatus());
			return;
		}
		if (subscriptionRepository.existsByExternalRef(req.getExternalRef())) {
			return;
		}
		UserEntity user = userRepository.findById(req.getUserId())
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		BusinessEntity business = businessRepository.findById(req.getBusinessId())
				.orElseThrow(() -> new ServiceException("Business not found", HttpStatus.NOT_FOUND));
		if (!"PUBLIC".equalsIgnoreCase(business.getOnboardingSource())
				&& !"SOCIAL".equalsIgnoreCase(business.getOnboardingSource())) {
			throw new ServiceException("Subscription bypass applies only to public registration.", HttpStatus.BAD_REQUEST);
		}

		business.setStatus("ACTIVE");
		business.setSubscriptionStatus("PAID");
		business.setSubscriptionVerifiedAt(LocalDateTime.now());
		businessRepository.save(business);

		user.setStatus("ACTIVE");
		user.setExpiresAt(null);
		userRepository.save(user);

		UserSubscriptionEntity sub = new UserSubscriptionEntity();
		sub.setUserId(user.getId());
		sub.setBusinessId(business.getId());
		sub.setExternalRef(req.getExternalRef().trim());
		sub.setStatus("PAID");
		sub.setPaidAt(LocalDateTime.now());
		sub.setCreatedAt(LocalDateTime.now());
		subscriptionRepository.save(sub);

		log.info("[SUBSCRIPTION_WEBHOOK] activated businessId={} userId={} ref={}",
				business.getId(), user.getId(), req.getExternalRef());
	}
}
