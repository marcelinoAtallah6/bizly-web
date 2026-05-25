package com.auth.api.service.registration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import javax.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.auth.api.controllers.dto.registration.AssignableRoleDto;
import com.auth.api.controllers.dto.registration.BusinessTypeOption;
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
import com.auth.common.PasswordUtil;
import com.auth.config.Exception.ServiceException;

/**
 * Implements the secure business-registration flow.
 *
 * <p>Business type and user role are the same concept: each business type
 * (e.g. {@code RESTAURANT}, {@code CLINIC}, {@code BEAUTY_CENTER}, {@code GYM},
 * {@code TAXI_COMPANY}) is a row in {@code um_role} flagged
 * {@code is_business_type = 1}. When a user signs up they pick a business
 * type from {@code POST /auth/business-types}; the chosen row's id becomes
 * the new user's role AND is cached on {@code um_business.business_type}.</p>
 *
 * <p>Critical invariants enforced here:</p>
 * <ol>
 *   <li>The client sends {@code roleId} (or a legacy free-form
 *       {@code businessType} name). Either way the server re-resolves it on
 *       the backend and refuses anything that isn't a BUSINESS-level,
 *       non-system, business-type role. SUPER_ADMIN can never be chosen.</li>
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
	@Autowired private BusinessRegistrationWorkflowService businessRegistrationWorkflowService;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private PasswordUtil passwordUtil;
	@Autowired private RegistrationPolicyService registrationPolicyService;

	@Value("${app.registration.require-super-admin-business-approval:true}")
	private boolean requireSuperAdminBusinessApproval;

	@PostConstruct
	void logRegistrationConfig() {
		log.info("[REGISTER][CONFIG] requireSuperAdminBusinessApproval={}", requireSuperAdminBusinessApproval);
	}

	/** Mirrors {@code ApiDefaultValdiation} server-side: at least 8 chars, upper, lower, digit, special. */
	private static final Pattern STRONG_PWD = Pattern
			.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

	private static final Pattern EMAIL_RX = Pattern
			.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	private static final String DEFAULT_USER_STATUS = "ACTIVE";
	private static final String DEFAULT_MOBILE_PLACEHOLDER = "-";

	@Override
	@Transactional
	public RegisterResponse register(RegisterRequest req, String deviceId, String ip) {

		if (req == null) {
			throw new ServiceException(ApiMessages.USERNAME_REQUIRED, HttpStatus.BAD_REQUEST);
		}

		String username = clean(req.getUsername());
		String email    = clean(req.getEmail());
		String password = req.getPassword();
		String confirmPassword = req.getConfirmPassword();
		String firstName = clean(req.getFirstName());
		String lastName  = clean(req.getLastName());
		String businessName = req.getBusinessName() != null ? req.getBusinessName().trim() : null;
		Long roleId = req.getRoleId();
		String businessTypeLegacy = req.getBusinessType() != null ? req.getBusinessType().trim() : null;
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
		if (!isSocial) {
			if (password == null || password.isEmpty()) {
				throw new ServiceException(ApiMessages.PASSWORD_REQUIRED, HttpStatus.BAD_REQUEST);
			}
			if (confirmPassword == null || confirmPassword.isEmpty()) {
				throw new ServiceException(ApiMessages.PASSWORD_REQUIRED, HttpStatus.BAD_REQUEST);
			}
			String plainPwd;
			String plainConfirm;
			try {
				plainPwd = passwordUtil.decryptPassword(password);
				plainConfirm = passwordUtil.decryptPassword(confirmPassword);
			} catch (Exception e) {
				log.warn("[REGISTER][PASSWORD_DECRYPT_FAIL] user={} err={}", username, e.getMessage());
				throw new ServiceException(ApiMessages.PASSWORD_PROCESSING_FAILED, HttpStatus.BAD_REQUEST);
			}
			if (!plainPwd.equals(plainConfirm)) {
				throw new ServiceException(ApiMessages.PASSWORDS_DO_NOT_MATCH, HttpStatus.BAD_REQUEST);
			}
			if (!STRONG_PWD.matcher(plainPwd).matches()) {
				throw new ServiceException(ApiMessages.PASSWORD_TOO_WEAK, HttpStatus.BAD_REQUEST);
			}
			password = plainPwd;
		}
		if (businessName == null || businessName.isEmpty()) {
			throw new ServiceException(ApiMessages.BUSINESS_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
		}
		if (businessName.length() > 200) {
			businessName = businessName.substring(0, 200);
		}

		if (userRepository.findFirstByUsernameOrderByIdAsc(username).isPresent()) {
			throw new ServiceException(ApiMessages.USERNAME_TAKEN, HttpStatus.CONFLICT);
		}
		if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
			throw new ServiceException(ApiMessages.EMAIL_TAKEN, HttpStatus.CONFLICT);
		}
		if (businessRepository.existsByBusinessNameIgnoreCase(businessName)) {
			throw new ServiceException(ApiMessages.BUSINESS_NAME_TAKEN, HttpStatus.CONFLICT);
		}

		RoleEntity role = resolveBusinessTypeRole(roleId, businessTypeLegacy, username);

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
		String registrationSource = isSocial ? "SOCIAL" : "PUBLIC";
		user.setUserType("BUSINESS_OWNER");
		user.setRegistrationSource(registrationSource);
		user.setIsBusinessOwner(1);
		user.setStatus(DEFAULT_USER_STATUS);
		user.setExpiresAt(LocalDateTime.now().plusDays(registrationPolicyService.getPublicRegistrationExpiryDays()));
		String mobile = clean(req.getMobileNumber());
		user.setMobileNumber(mobile != null ? mobile : DEFAULT_MOBILE_PLACEHOLDER);
		user = userRepository.save(user);

		// ---- Create business and link ----
		BusinessEntity business = new BusinessEntity();
		business.setBusinessName(businessName);
		business.setBusinessType(role.getName());
		business.setOnboardingSource(registrationSource);
		business.setBusinessTypeRoleId(role.getId());
		String initialStatus = resolveInitialBusinessStatus(registrationSource);
		business.setStatus(initialStatus);
		business.setCreatedBy(user.getId());
		business.setCreatedAt(LocalDateTime.now());
		BusinessEntity savedBiz = businessRepository.save(business);

		user.setBusinessId(savedBiz.getId());
		userRepository.save(user);

		ensureUserRole(user.getId(), role);

		if ("PENDING_APPROVAL".equalsIgnoreCase(savedBiz.getStatus())) {
			businessRegistrationWorkflowService.createPendingInstanceIfNeeded(savedBiz.getId(),
					savedBiz.getBusinessName(), user.getId(), username);
		}

		log.info("[REGISTER][OK] user={} businessId={} businessStatus={} requireSuperAdminBusinessApproval={} role={} provider={}",
				username, savedBiz.getId(), savedBiz.getStatus(), requireSuperAdminBusinessApproval,
				role.getName(), user.getAuthProvider());

		LoginResponse session = loginService.reissueAccessTokenForUser(user.getId(), deviceId, ip);
		log.info("[REGISTER][SESSION] user={} pendingBusinessApproval={} businessStatus={} tokenRoles={}",
				username, session.getPendingBusinessApproval(), session.getBusinessStatus(),
				session.getAvailableRoles());

		RegisterResponse out = new RegisterResponse();
		out.setUserId(user.getId());
		out.setUsername(user.getUsername());
		out.setBusinessId(savedBiz.getId());
		out.setBusinessName(savedBiz.getBusinessName());
		out.setSession(session);
		return out;
	}

	private String resolveInitialBusinessStatus(String onboardingSource) {
		if ("ADMIN_PORTAL".equalsIgnoreCase(onboardingSource)) {
			return "ACTIVE";
		}
		return registrationPolicyService.requiresApprovalForPublicRegistration() ? "PENDING_APPROVAL" : "ACTIVE";
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

		UserEntity user = userRepository.findFirstByUsernameOrderByIdAsc(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

		if (user.getBusinessId() != null) {
			log.warn("[SECURITY_EVENT][REGISTER_BUSINESS_REJECTED] reason=already_registered user={} currentBusiness={}",
					username, user.getBusinessId());
			throw new ServiceException(ApiMessages.BUSINESS_ALREADY_REGISTERED, HttpStatus.CONFLICT);
		}

		if (businessRepository.existsByBusinessNameIgnoreCase(trimmedName)) {
			throw new ServiceException(ApiMessages.BUSINESS_NAME_TAKEN, HttpStatus.CONFLICT);
		}

		RoleEntity defaultRole = resolveBusinessTypeRole(req.getRoleId(), req.getBusinessType(), username);

		BusinessEntity business = new BusinessEntity();
		business.setBusinessName(trimmedName);
		business.setBusinessType(defaultRole.getName());
		business.setOnboardingSource("SOCIAL");
		business.setBusinessTypeRoleId(defaultRole.getId());
		String initialStatus = resolveInitialBusinessStatus("SOCIAL");
		business.setStatus(initialStatus);
		business.setCreatedBy(user.getId());
		business.setCreatedAt(LocalDateTime.now());
		BusinessEntity saved = businessRepository.save(business);
		user.setUserType("BUSINESS_OWNER");
		user.setRegistrationSource("SOCIAL");
		user.setIsBusinessOwner(1);
		user.setExpiresAt(LocalDateTime.now().plusDays(registrationPolicyService.getPublicRegistrationExpiryDays()));
		log.info("[REGISTER_BUSINESS][OK] user={} businessId={} businessStatus={} approvalEnabled={}",
				username, saved.getId(), saved.getStatus(), registrationPolicyService.isApprovalFlowEnabled());

		user.setBusinessId(saved.getId());
		user.setFirstLogin(0);
		user.setWelcomeCompletedAt(LocalDateTime.now());
		userRepository.save(user);

		ensureUserRole(user.getId(), defaultRole);

		if ("PENDING_APPROVAL".equalsIgnoreCase(saved.getStatus())) {
			businessRegistrationWorkflowService.createPendingInstanceIfNeeded(saved.getId(), saved.getBusinessName(),
					user.getId(), username);
		}

		LoginResponse fresh = loginService.reissueAccessTokenForUser(user.getId(), deviceId, ip);

		RegisterBusinessResponse out = new RegisterBusinessResponse();
		out.setBusinessId(saved.getId());
		out.setBusinessName(saved.getBusinessName());
		out.setRequiresBusinessApprovalWorkflow(!"ACTIVE".equalsIgnoreCase(saved.getStatus()));
		out.setSession(fresh);
		return out;
	}

	@Override
	public List<BusinessTypeOption> listBusinessTypes() {
		// Build the assignable level set once so we don't re-query inside the loop.
		List<Long> assignableLevelIds = new ArrayList<>();
		for (RoleLevelEntity level : roleLevelRepository.findAll()) {
			if (level.isAssignableOnRegistration()) {
				assignableLevelIds.add(level.getId());
			}
		}
		List<BusinessTypeOption> out = new ArrayList<>();
		if (assignableLevelIds.isEmpty()) {
			return out;
		}
		for (RoleEntity role : roleRepository.findAll()) {
			if (!role.isBusinessType()) continue;
			if (role.isSystemRestricted()) continue;
			if (role.getRoleLevelId() == null || !assignableLevelIds.contains(role.getRoleLevelId())) continue;
			out.add(new BusinessTypeOption(role.getId(), role.getName(), prettify(role.getName())));
		}
		out.sort((a, b) -> a.getName() == null ? -1 : a.getName().compareToIgnoreCase(b.getName() == null ? "" : b.getName()));
		return out;
	}

	/** "BEAUTY_CENTER" -> "Beauty Center". Used purely as a default display label. */
	private static String prettify(String name) {
		if (name == null || name.isBlank()) {
			return name;
		}
		String[] parts = name.trim().toLowerCase(Locale.ROOT).split("[\\s_\\-]+");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			String p = parts[i];
			if (p.isEmpty()) continue;
			if (sb.length() > 0) sb.append(' ');
			sb.append(Character.toUpperCase(p.charAt(0)));
			if (p.length() > 1) sb.append(p.substring(1));
		}
		return sb.length() == 0 ? name : sb.toString();
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
		UserEntity user = userRepository.findFirstByUsernameOrderByIdAsc(username)
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
		UserEntity user = userRepository.findFirstByUsernameOrderByIdAsc(username)
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
			businessRepository.findById(user.getBusinessId()).ifPresent(b -> {
				me.setBusinessName(b.getBusinessName());
				me.setBusinessStatus(b.getStatus());
				me.setPendingBusinessApproval("PENDING_APPROVAL".equalsIgnoreCase(b.getStatus()));
			});
		}
		me.setRoles(roles);
		me.setRoleLevel(roleLevel);
		me.setCanRegisterBusiness(user.getBusinessId() == null && !"ADMIN".equalsIgnoreCase(roleLevel));
		me.setMobileNumber(user.getMobileNumber());
		byte[] img = user.getProfileImageData();
		String imgMime = user.getProfileImageMime();
		if (img != null && img.length > 0 && imgMime != null && !imgMime.isBlank()) {
			String b64 = Base64.getEncoder().encodeToString(img);
			/* /auth/me is not the JWT — allow a larger snapshot than the token embed budget so the SPA
			 * profile form can render without a second round trip; still cap to keep payloads sane. */
			if (b64.length() <= 200_000) {
				me.setProfileImageMime(imgMime.trim());
				me.setProfileImageBase64(b64);
			}
		}
		return me;
	}

	@Override
	public MeAvatarResponse meAvatar(String username) {
		UserEntity user = userRepository.findFirstByUsernameOrderByIdAsc(username)
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
	 * Resolves the role the user is asking to be granted at sign-up. The client
	 * sends either {@code roleId} (preferred — picked from
	 * {@code /auth/business-types}) or a legacy {@code businessType} name. We
	 * RE-VALIDATE every attribute server-side before returning, so a tampered
	 * client cannot promote itself to SUPER_ADMIN even if it knows a valid id.
	 *
	 * <p>Accepted role criteria:</p>
	 * <ul>
	 *   <li>Must be flagged {@code is_business_type = 1}.</li>
	 *   <li>Must NOT be flagged {@code is_system_restricted = 1}.</li>
	 *   <li>Its {@code role_level} must be assignable on registration
	 *       (= BUSINESS-level in the canonical seed data).</li>
	 * </ul>
	 *
	 * <p>If the client sends neither field, or sends an id that fails any of the
	 * above checks, we log a {@code SECURITY_EVENT} and refuse the request.</p>
	 */
	private RoleEntity resolveBusinessTypeRole(Long roleId, String legacyName, String username) {
		RoleEntity role = null;

		if (roleId != null) {
			role = roleRepository.findById(roleId).orElse(null);
			if (role == null) {
				log.warn("[SECURITY_EVENT][REGISTER_REJECTED] reason=role_id_not_found user={} roleId={}",
						username, roleId);
				throw new ServiceException(ApiMessages.BUSINESS_TYPE_INVALID, HttpStatus.BAD_REQUEST);
			}
		} else if (legacyName != null && !legacyName.isBlank()) {
			// Legacy clients sent the role name. Look it up case-insensitively.
			role = roleRepository.findFirstByNameIgnoreCaseOrderByIdAsc(legacyName.trim()).orElse(null);
			if (role == null) {
				log.warn("[SECURITY_EVENT][REGISTER_REJECTED] reason=role_name_not_found user={} name={}",
						username, legacyName);
				throw new ServiceException(ApiMessages.BUSINESS_TYPE_INVALID, HttpStatus.BAD_REQUEST);
			}
		} else {
			log.warn("[SECURITY_EVENT][REGISTER_REJECTED] reason=missing_role user={}", username);
			throw new ServiceException(ApiMessages.BUSINESS_TYPE_REQUIRED, HttpStatus.BAD_REQUEST);
		}

		if (role.isSystemRestricted()) {
			log.warn("[SECURITY_EVENT][REGISTER_REJECTED] reason=role_system_restricted user={} role={}",
					username, role.getName());
			throw new ServiceException(ApiMessages.ROLE_NOT_ASSIGNABLE, HttpStatus.FORBIDDEN);
		}
		if (!role.isBusinessType()) {
			log.warn("[SECURITY_EVENT][REGISTER_REJECTED] reason=role_not_business_type user={} role={}",
					username, role.getName());
			throw new ServiceException(ApiMessages.ROLE_NOT_ASSIGNABLE, HttpStatus.FORBIDDEN);
		}
		Optional<RoleLevelEntity> level = role.getRoleLevelId() == null ? Optional.empty()
				: roleLevelRepository.findById(role.getRoleLevelId());
		if (level.isEmpty() || !level.get().isAssignableOnRegistration()) {
			log.warn("[SECURITY_EVENT][REGISTER_REJECTED] reason=role_level_not_assignable user={} role={}",
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

	/**
	 * {@code /auth/me} role-level resolver. Scans every assigned role and PREFERS one that maps to
	 * {@code role_level.code = 'ADMIN'} (DB linkage first, then canonical name fallback). Returns
	 * {@code null} only when no assigned role resolves to a known level. Without this preference a
	 * super-admin who also holds a tenant role would be reported as {@code BUSINESS} and bounced
	 * through registration on first load.
	 */
	private String resolveRoleLevelForFirstRole(Long userId) {
		List<String> roleNames = userRoleRepository.findRoleNamesByUserId(userId);
		if (roleNames.isEmpty()) {
			return null;
		}
		String firstCode = null;
		for (String raw : roleNames) {
			String name = raw == null ? "" : raw.trim();
			if (name.isEmpty()) {
				continue;
			}
			Optional<RoleEntity> hit = roleRepository.findFirstByNameIgnoreCaseOrderByIdAsc(name);
			if (hit.isEmpty()) {
				continue;
			}
			Long levelId = hit.get().getRoleLevelId();
			String code = null;
			if (levelId != null) {
				code = roleLevelRepository.findById(levelId).map(RoleLevelEntity::getCode).orElse(null);
			}
			if (code == null || code.isBlank()) {
				code = inferAdminLevelFromRoleFlags(hit.get());
			}
			if ("ADMIN".equalsIgnoreCase(code)) {
				return code;
			}
			if (firstCode == null && code != null && !code.isBlank()) {
				firstCode = code;
			}
		}
		return firstCode;
	}

	private static String inferAdminLevelFromRoleFlags(RoleEntity role) {
		if (role != null && role.isSystemRestricted() && !role.isBusinessType()) {
			return RoleLevelEntity.CODE_ADMIN;
		}
		return null;
	}
}
