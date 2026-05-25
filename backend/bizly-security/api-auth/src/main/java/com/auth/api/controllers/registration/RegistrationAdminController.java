package com.auth.api.controllers.registration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.api.controllers.dto.registration.AdminOnboardBusinessRequest;
import com.auth.api.controllers.dto.registration.AdminOnboardBusinessResponse;
import com.auth.api.controllers.dto.registration.PlatformConfigResponse;
import com.auth.api.controllers.dto.registration.SubscriptionWebhookRequest;
import com.auth.api.controllers.dto.registration.VerifyEmailSetPasswordRequest;
import com.auth.api.model.config.PlatformConfigEntity;
import com.auth.api.repository.config.PlatformConfigRepository;
import com.auth.api.service.registration.AdminBusinessOnboardingService;
import com.auth.api.service.registration.EmailVerificationService;
import com.auth.api.service.registration.SubscriptionActivationService;
import com.auth.config.Exception.ServiceException;
import com.auth.config.common.ApiResponse;

@RestController
@RequestMapping("/auth")
public class RegistrationAdminController {

	@Autowired private PlatformConfigRepository platformConfigRepository;
	@Autowired private AdminBusinessOnboardingService adminBusinessOnboardingService;
	@Autowired private EmailVerificationService emailVerificationService;
	@Autowired private SubscriptionActivationService subscriptionActivationService;

	@Value("${app.subscription.webhook-secret:}")
	private String webhookSecret;

	@GetMapping("/platform-config")
	public ResponseEntity<ApiResponse<PlatformConfigResponse>> platformConfig() {
		PlatformConfigEntity cfg = platformConfigRepository.findById(1L).orElse(new PlatformConfigEntity());
		PlatformConfigResponse out = new PlatformConfigResponse();
		out.setApprovalFlowEnabled(cfg.isApprovalFlowEnabled());
		out.setSubscriptionFlowEnabled(cfg.isSubscriptionFlowEnabled());
		out.setPublicRegistrationExpiryDays(cfg.getPublicRegistrationExpiryDays() != null
				? cfg.getPublicRegistrationExpiryDays() : 30);
		return ResponseEntity.ok(ApiResponse.success(out, "OK"));
	}

	@PostMapping("/admin/business/onboard")
	public ResponseEntity<ApiResponse<AdminOnboardBusinessResponse>> onboardBusiness(
			@RequestBody AdminOnboardBusinessRequest req, HttpServletRequest request) {
		requirePortalAdmin(request);
		return ResponseEntity.ok(ApiResponse.success(adminBusinessOnboardingService.onboard(req),
				"Business onboarded; verification email sent"));
	}

	@PostMapping("/verify-email/set-password")
	public ResponseEntity<ApiResponse<String>> verifyEmailSetPassword(@RequestBody VerifyEmailSetPasswordRequest req) {
		emailVerificationService.verifyAndSetPassword(req.getToken(), req.getPassword());
		return ResponseEntity.ok(ApiResponse.success("OK", "Email verified and password set"));
	}

	/**
	 * Payment provider webhook — configure your provider URL to POST here.
	 * Send header {@code X-Webhook-Secret} matching {@code app.subscription.webhook-secret}.
	 */
	@PostMapping("/webhooks/payment/subscription")
	public ResponseEntity<ApiResponse<String>> subscriptionWebhook(
			@RequestBody SubscriptionWebhookRequest req, HttpServletRequest request) {
		assertWebhookSecret(request);
		subscriptionActivationService.handlePaymentWebhook(req);
		return ResponseEntity.ok(ApiResponse.success("OK", "Processed"));
	}

	private void requirePortalAdmin(HttpServletRequest request) {
		String roleLevel = request.getHeader("X-Role-Level");
		if (roleLevel == null || !"ADMIN".equalsIgnoreCase(roleLevel.trim())) {
			throw new ServiceException("Portal admin access required", HttpStatus.FORBIDDEN);
		}
	}

	private void assertWebhookSecret(HttpServletRequest request) {
		if (webhookSecret == null || webhookSecret.isBlank()) {
			return;
		}
		String provided = request.getHeader("X-Webhook-Secret");
		if (provided == null || !webhookSecret.equals(provided)) {
			throw new ServiceException("Invalid webhook secret", HttpStatus.UNAUTHORIZED);
		}
	}
}
