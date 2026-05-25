package com.auth.api.service.registration;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.api.controllers.dto.registration.AdminOnboardBusinessRequest;
import com.auth.api.controllers.dto.registration.AdminOnboardBusinessResponse;
import com.auth.api.model.business.BusinessEntity;
import com.auth.api.model.user.RoleEntity;
import com.auth.api.model.user.UserEntity;
import com.auth.api.model.user.UserRoleEntity;
import com.auth.api.model.user.UserRoleId;
import com.auth.api.repository.business.BusinessRepository;
import com.auth.api.repository.user.RoleRepository;
import com.auth.api.repository.user.UserRepository;
import com.auth.api.repository.user.UserRoleRepository;
import com.auth.common.ApiMessages;
import com.auth.config.Exception.ServiceException;

/**
 * Portal admin creates a business + owner. No approval workflow; owner verifies email and sets password.
 */
@Service
public class AdminBusinessOnboardingService {

	private static final Logger log = LoggerFactory.getLogger(AdminBusinessOnboardingService.class);

	@Autowired private UserRepository userRepository;
	@Autowired private BusinessRepository businessRepository;
	@Autowired private RoleRepository roleRepository;
	@Autowired private UserRoleRepository userRoleRepository;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private EmailVerificationService emailVerificationService;

	@Transactional
	public AdminOnboardBusinessResponse onboard(AdminOnboardBusinessRequest req) {
		if (req == null || req.getBusinessName() == null || req.getBusinessName().isBlank()) {
			throw new ServiceException(ApiMessages.BUSINESS_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
		}
		if (req.getEmail() == null || req.getEmail().isBlank()) {
			throw new ServiceException(ApiMessages.EMAIL_REQUIRED, HttpStatus.BAD_REQUEST);
		}
		String businessName = req.getBusinessName().trim();
		if (businessRepository.existsByBusinessNameIgnoreCase(businessName)) {
			throw new ServiceException(ApiMessages.BUSINESS_NAME_TAKEN, HttpStatus.CONFLICT);
		}
		RoleEntity template = roleRepository.findById(req.getBusinessTypeRoleId())
				.orElseThrow(() -> new ServiceException(ApiMessages.ROLE_NOT_FOUND, HttpStatus.BAD_REQUEST));
		if (!template.isBusinessType()) {
			throw new ServiceException("Selected role is not a business type template.", HttpStatus.BAD_REQUEST);
		}

		String username = allocateUsername(req.getEmail());
		UserEntity owner = new UserEntity();
		owner.setUsername(username);
		owner.setEmail(req.getEmail().trim());
		owner.setFirstName(req.getFirstName() != null ? req.getFirstName().trim() : "");
		owner.setLastName(req.getLastName() != null ? req.getLastName().trim() : "");
		owner.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
		owner.setStatus("PENDING_EMAIL_VERIFICATION");
		owner.setUserType("BUSINESS_OWNER");
		owner.setRegistrationSource("ADMIN_PORTAL");
		owner.setIsBusinessOwner(1);
		owner.setFirstLogin(1);
		owner.setAuthProvider("LOCAL");
		owner.setMobileNumber("-");
		owner.setFailedLoginAttempts(0);
		owner.setAccountLocked(false);
		owner = userRepository.save(owner);

		BusinessEntity business = new BusinessEntity();
		business.setBusinessName(businessName);
		business.setBusinessType(template.getName());
		business.setStatus("ACTIVE");
		business.setOnboardingSource("ADMIN_PORTAL");
		business.setBusinessTypeRoleId(template.getId());
		business.setCreatedBy(owner.getId());
		business.setCreatedAt(LocalDateTime.now());
		business = businessRepository.save(business);

		owner.setBusinessId(business.getId());
		userRepository.save(owner);

		UserRoleEntity ur = new UserRoleEntity();
		UserRoleId urId = new UserRoleId();
		urId.setUserId(owner.getId());
		urId.setRoleId(template.getId());
		ur.setId(urId);
		userRoleRepository.save(ur);

		emailVerificationService.sendOwnerInvite(owner, business.getBusinessName());
		log.info("[ADMIN_ONBOARD] businessId={} owner={} template={}", business.getId(), username, template.getName());

		AdminOnboardBusinessResponse out = new AdminOnboardBusinessResponse();
		out.setBusinessId(business.getId());
		out.setBusinessName(business.getBusinessName());
		out.setOwnerUserId(owner.getId());
		out.setOwnerUsername(username);
		return out;
	}

	private String allocateUsername(String email) {
		int at = email.indexOf('@');
		String base = at > 0 ? email.substring(0, at) : email;
		base = base.replaceAll("[^a-zA-Z0-9.]", "");
		if (base.length() < 4) {
			base = base + "user";
		}
		String candidate = base;
		int n = 2;
		while (userRepository.findFirstByUsernameOrderByIdAsc(candidate).isPresent()) {
			candidate = base + n++;
		}
		return candidate;
	}
}
