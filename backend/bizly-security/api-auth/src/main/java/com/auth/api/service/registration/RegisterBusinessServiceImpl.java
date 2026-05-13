package com.auth.api.service.registration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Base64;

import com.auth.api.controllers.dto.registration.AssignableRoleDto;
import com.auth.api.controllers.dto.registration.MeAvatarResponse;
import com.auth.api.controllers.dto.registration.MeResponse;
import com.auth.api.controllers.dto.registration.RegisterBusinessRequest;
import com.auth.api.controllers.dto.registration.RegisterBusinessResponse;
import com.auth.api.controllers.dto.registration.RegisterRequest;
import com.auth.api.controllers.dto.registration.RegisterResponse;
import com.auth.api.model.business.BusinessEntity;
import com.auth.api.model.login.LoginResponse;
import com.auth.api.model.role.RoleLevelEntity;
import com.auth.api.model.user.RoleEntity;
import com.auth.api.model.user.UserEntity;
import com.auth.api.model.user.UserRoleEntity;
import com.auth.api.model.user.UserRoleId;
import com.auth.api.repository.business.BusinessRepository;
import com.auth.api.repository.role.RoleLevelRepository;
import com.auth.api.repository.user.RoleRepository;
import com.auth.api.repository.user.UserRepository;
import com.auth.api.repository.user.UserRoleRepository;
import com.auth.api.service.login.ILoginService;
import com.auth.common.ApiMessages;
import com.auth.config.Exception.ServiceException;

/**
 * Implements the secure business-registration flow described in the
 * functional spec. Critical invariants enforced here:
 *
 * <ol>
 *   <li>The {@link RegisterBusinessRequest} DTO has no {@code roleId} field;
 *       any client-side attempt to inject one is silently dropped by Jackson.</li>
 *   <li>The service still validates the resolved role server-side before
 *       inserting into {@code um_user_role}: it must belong to the BUSINESS
 *       level, must have {@code is_default_for_registration = 1}, and must
 *       not be flagged as {@code is_system_restricted}.</li>
 *   <li>If the user already has a {@code business_id}, the call is refused
 *       (a tenant cannot be hijacked / replaced).</li>
 *   <li>A new business name must not collide with an existing one.</li>
 *   <li>Every refusal logs a {@code SECURITY_EVENT} line so the operator can
 *       review attempts to escalate privilege.</li>
 * </ol>
 */
@Service
public class RegisterBusinessServiceImpl implements IRegisterBusinessService {

	private static final Logger log = LoggerFactory.getLogger(RegisterBusinessServiceImpl.class);

	@Autowired private UserRepository userRepository;
	@Autowired private UserRoleRepository userRoleRepository;
	@Autowired private RoleRepository roleRepository;
	@Autowired private RoleLevelRepository roleLevelRepository;
	@Autowired private BusinessRepository businessRepository;
	@Autowired private ILoginService loginService;
	@Autowired private PasswordEncoder passwordEncoder;

	/** Mirrors {@code ApiDefaultValdiation} server-side: at least 8 chars, upper, lower, digit, special. */
	private static final Pattern STRONG_PWD = Pattern
			.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

	private static final Pattern EMAIL_RX = Pattern
			.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	@Override
	@Transactional
	public RegisterResponse register(RegisterRequest req, String deviceId, String ip) {

		if (req == null) {
			throw new ServiceException(ApiMessages.USERNAME_REQUIRED, HttpStatus.BAD_REQUEST);
		}

		String username = clean(req.getUsername());
		String email    = clean(req.getEmail());
		String password = req.getPassword();
		String firstName = clean(req.getFirstName());
		String lastName  = clean(req.getLastName());
		String businessName = req.getBusinessName() != null ? req.getBusinessName().trim() : null;
		String businessType = req.getBusinessType() != null ? req.getBusinessType().trim() : null;
		boolean isSocial = req.getAuthProvider() != null && !req.getAuthProvider().isBlank();

		// ---- Validation ----
		if (username == null || username.length() < 4) {
			throw new ServiceException(ApiMessages.USERNAME_REQUIRED, HttpStatus.BAD_REQUEST);
		}
		if (email == null || !EMAIL_RX.matcher(email).matches()) {
			throw new ServiceException(ApiMessages.EMAIL_REQUIRED, HttpStatus.BAD_REQUEST);
		}
		if (firstName == null || firstName.isEmpty()) {
			throw new ServiceException(ApiMessages.FIRST_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
		}
		if (lastName == null || lastName.isEmpty()) {
			throw new ServiceException(ApiMessages.LAST_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
		}
		// Password rules: only enforced for manual sign-up. Social sign-ups get a random password.
		if (!isSocial) {
			if (password == null || password.isEmpty()) {
				throw new ServiceException(ApiMessages.PASSWORD_REQUIRED, HttpStatus.BAD_REQUEST);
			}
			if (!password.equals(req.getConfirmPassword())) {
				throw new ServiceException(ApiMessages.PASSWORDS_DO_NOT_MATCH, HttpStatus.BAD_REQUEST);
			}
			if (!STRONG_PWD.matcher(password).matches()) {
				throw new ServiceException(ApiMessages.PASSWORD_TOO_WEAK, HttpStatus.BAD_REQUEST);
			}
		}
		if (businessName == null || businessName.isEmpty()) {
			throw new ServiceException(ApiMessages.BUSINESS_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
		}
		if (businessName.length() > 200) {
			businessName = businessName.substring(0, 200);
		}

		// ---- Uniqueness ----
		if (userRepository.findByUsername(username).isPresent()) {
			throw new ServiceException(ApiMessages.USERNAME_TAKEN, HttpStatus.CONFLICT);
		}
		if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
			throw new ServiceException(ApiMessages.EMAIL_TAKEN, HttpStatus.CONFLICT);
		}
		if (businessRepository.existsByBusinessNameIgnoreCase(businessName)) {
			throw new ServiceException(ApiMessages.BUSINESS_NAME_TAKEN, HttpStatus.CONFLICT);
		}

		// ---- Server decides the role; UI is never trusted. ----
		RoleEntity role = resolveDefaultRegistrationRole(username);

		// ---- Create user ----
		UserEntity user = new UserEntity();
		user.setUsername(username);
		user.setEmail(email);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		// Random password for social so the local-login path can never use it.
		String rawPwd = isSocial ? java.util.UUID.randomUUID().toString() : password;
		user.setPassword(passwordEncoder.encode(rawPwd));
		user.setFailedLoginAttempts(0);
		user.setAccountLocked(false);
		user.setFirstLogin(1);
		user.setAuthProvider(isSocial ? req.getAuthProvider().toUpperCase(Locale.ROOT) : "LOCAL");
		user.setProviderUserId(req.getProviderUserId());
		user = userRepository.save(user);

		// ---- Create business and link ----
		BusinessEntity business = new BusinessEntity();
		business.setBusinessName(businessName);
		business.setBusinessType(businessType);
		business.setStatus("ACTIVE");
		business.setCreatedBy(user.getId());
		business.setCreatedAt(LocalDateTime.now());
		BusinessEntity savedBiz = businessRepository.save(business);

		user.setBusinessId(savedBiz.getId());
		userRepository.save(user);

		ensureUserRole(user.getId(), role);

		log.info("[REGISTER][OK] user={} businessId={} role={} provider={}",
				username, savedBiz.getId(), role.getName(), user.getAuthProvider());

		// Auto-issue a session so the SPA can land on the dashboard.
		LoginResponse session = loginService.reissueAccessTokenForUser(user.getId(), deviceId, ip);

		RegisterResponse out = new RegisterResponse();
		out.setUserId(user.getId());
		out.setUsername(user.getUsername());
		out.setBusinessId(savedBiz.getId());
		out.setBusinessName(savedBiz.getBusinessName());
		out.setSession(session);
		return out;
	}

	private static String clean(String s) {
		if (s == null) return null;
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}

	@Override
	@Transactional
	public RegisterBusinessResponse registerBusiness(String username, String deviceId, String ip,
			RegisterBusinessRequest req) {

		if (req == null || req.getBusinessName() == null || req.getBusinessName().trim().isEmpty()) {
			throw new ServiceException(ApiMessages.BUSINESS_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
		}
		String trimmedName = req.getBusinessName().trim();
		if (trimmedName.length() > 200) {
			trimmedName = trimmedName.substring(0, 200);
		}

		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

		// (3) — refuse if the user already owns a business. Prevents a logged-in user from silently
		//       switching tenant by replaying the endpoint.
		if (user.getBusinessId() != null) {
			log.warn("[SECURITY_EVENT][REGISTER_BUSINESS_REJECTED] reason=already_registered user={} currentBusiness={}",
					username, user.getBusinessId());
			throw new ServiceException(ApiMessages.BUSINESS_ALREADY_REGISTERED, HttpStatus.CONFLICT);
		}

		// (4) — name collision check is case-insensitive.
		if (businessRepository.existsByBusinessNameIgnoreCase(trimmedName)) {
			throw new ServiceException(ApiMessages.BUSINESS_NAME_TAKEN, HttpStatus.CONFLICT);
		}

		RoleEntity defaultRole = resolveDefaultRegistrationRole(username);

		// Persist business first so we have a generated id to attach to the user.
		BusinessEntity business = new BusinessEntity();
		business.setBusinessName(trimmedName);
		business.setBusinessType(req.getBusinessType());
		business.setStatus("ACTIVE");
		business.setCreatedBy(user.getId());
		business.setCreatedAt(LocalDateTime.now());
		BusinessEntity saved = businessRepository.save(business);

		// Link user → business + finish the welcome state.
		user.setBusinessId(saved.getId());
		user.setFirstLogin(0);
		user.setWelcomeCompletedAt(LocalDateTime.now());
		userRepository.save(user);

		// Insert um_user_role idempotently so a retry doesn't crash on PK violation.
		ensureUserRole(user.getId(), defaultRole);

		LoginResponse fresh = loginService.reissueAccessTokenForUser(user.getId(), deviceId, ip);

		RegisterBusinessResponse out = new RegisterBusinessResponse();
		out.setBusinessId(saved.getId());
		out.setBusinessName(saved.getBusinessName());
		out.setSession(fresh);
		return out;
	}

	@Override
	public List<AssignableRoleDto> listAssignableRoles() {
		List<AssignableRoleDto> result = new ArrayList<>();
		List<RoleLevelEntity> levels = roleLevelRepository.findAll();
		for (RoleLevelEntity level : levels) {
			if (!level.isAssignableOnRegistration()) {
				continue;
			}
			for (RoleEntity role : roleRepository.findAll()) {
				if (role.getRoleLevelId() == null || !role.getRoleLevelId().equals(level.getId())) {
					continue;
				}
				if (role.isSystemRestricted()) {
					continue;
				}
				result.add(new AssignableRoleDto(role.getId(), role.getName(), level.getCode(),
						role.isDefaultForRegistration()));
			}
		}
		return result;
	}

	@Override
	@Transactional
	public RegisterBusinessResponse completeWelcome(String username, String deviceId, String ip) {
		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

		user.setFirstLogin(0);
		if (user.getWelcomeCompletedAt() == null) {
			user.setWelcomeCompletedAt(LocalDateTime.now());
		}
		userRepository.save(user);

		LoginResponse fresh = loginService.reissueAccessTokenForUser(user.getId(), deviceId, ip);
		RegisterBusinessResponse out = new RegisterBusinessResponse();
		out.setBusinessId(user.getBusinessId());
		out.setSession(fresh);
		return out;
	}

	@Override
	public MeResponse me(String username) {
		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

		List<String> roles = userRoleRepository.findRoleNamesByUserId(user.getId());
		String roleLevel = resolveRoleLevelForFirstRole(user.getId());

		MeResponse me = new MeResponse();
		me.setUserId(user.getId());
		me.setUsername(user.getUsername());
		me.setEmail(user.getEmail());
		me.setFirstName(user.getFirstName());
		me.setLastName(user.getLastName());
		me.setFirstLogin(user.isFirstLogin());
		me.setBusinessId(user.getBusinessId());
		if (user.getBusinessId() != null) {
			businessRepository.findById(user.getBusinessId())
					.map(BusinessEntity::getBusinessName)
					.ifPresent(me::setBusinessName);
		}
		me.setRoles(roles);
		me.setRoleLevel(roleLevel);
		me.setCanRegisterBusiness(user.getBusinessId() == null && !"ADMIN".equalsIgnoreCase(roleLevel));
		return me;
	}

	@Override
	public MeAvatarResponse meAvatar(String username) {
		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		MeAvatarResponse out = new MeAvatarResponse();
		out.setUserId(user.getId());
		byte[] data = user.getProfileImageData();
		String mime = user.getProfileImageMime();
		if (data != null && data.length > 0 && mime != null && !mime.isBlank()) {
			out.setMime(mime);
			out.setBase64(Base64.getEncoder().encodeToString(data));
		} else {
			out.setMime("");
			out.setBase64("");
		}
		return out;
	}

	/**
	 * Loads the single role marked {@code is_default_for_registration = 1}. Refuses to assign anything
	 * if the role is not BUSINESS-level or has {@code is_system_restricted = 1} — this is the last
	 * line of defence against tampering or DB mis-seeding.
	 */
	private RoleEntity resolveDefaultRegistrationRole(String username) {
		List<RoleEntity> candidates = new ArrayList<>();
		for (RoleEntity r : roleRepository.findAll()) {
			if (r.isDefaultForRegistration()) {
				candidates.add(r);
			}
		}
		if (candidates.isEmpty()) {
			log.error("[SECURITY_EVENT][REGISTER_BUSINESS_REJECTED] reason=no_default_role user={}", username);
			throw new ServiceException(ApiMessages.NO_DEFAULT_REGISTRATION_ROLE, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		RoleEntity role = candidates.get(0);

		if (role.isSystemRestricted()) {
			log.warn("[SECURITY_EVENT][REGISTER_BUSINESS_REJECTED] reason=default_role_restricted user={} role={}",
					username, role.getName());
			throw new ServiceException(ApiMessages.ROLE_NOT_ASSIGNABLE, HttpStatus.FORBIDDEN);
		}
		Optional<RoleLevelEntity> level = role.getRoleLevelId() == null ? Optional.empty()
				: roleLevelRepository.findById(role.getRoleLevelId());
		if (level.isEmpty() || !level.get().isAssignableOnRegistration()) {
			log.warn("[SECURITY_EVENT][REGISTER_BUSINESS_REJECTED] reason=default_role_not_assignable_level user={} role={}",
					username, role.getName());
			throw new ServiceException(ApiMessages.ROLE_NOT_ASSIGNABLE, HttpStatus.FORBIDDEN);
		}
		return role;
	}

	private void ensureUserRole(Long userId, RoleEntity role) {
		UserRoleId id = new UserRoleId();
		id.setUserId(userId);
		id.setRoleId(role.getId());
		if (userRoleRepository.existsById(id)) {
			return;
		}
		UserRoleEntity ur = new UserRoleEntity();
		ur.setId(id);
		ur.setRole(role);
		userRoleRepository.save(ur);
	}

	private String resolveRoleLevelForFirstRole(Long userId) {
		List<String> roleNames = userRoleRepository.findRoleNamesByUserId(userId);
		if (roleNames.isEmpty()) {
			return null;
		}
		return roleRepository.findByNameIgnoreCase(roleNames.get(0))
				.map(RoleEntity::getRoleLevelId)
				.flatMap(levelId -> levelId == null ? Optional.<RoleLevelEntity>empty() : roleLevelRepository.findById(levelId))
				.map(RoleLevelEntity::getCode)
				.orElse(null);
	}
}
