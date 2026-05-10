package com.auth.api.service.login;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import com.auth.api.controllers.dto.forgot.ForgotPasswordRequest;
import com.auth.api.controllers.dto.forgot.ResetPasswordRequest;
import com.auth.api.controllers.dto.forgot.VerifyResetTokenRequest;
import com.auth.api.controllers.dto.refresh.RefreshRequest;
import com.auth.api.controllers.dto.session.ActiveRoleRequest;
import com.auth.api.model.login.LoginResponse;
import com.auth.api.model.menu.UmMenuRouteEntity;
import com.auth.api.model.permission.RoleMenuPermissionEntity;
import com.auth.api.model.session.SessionEntity;
import com.auth.api.model.user.RoleEntity;
import com.auth.api.model.user.UserEntity;
import com.auth.api.repository.menu.UmMenuRouteRepository;
import com.auth.api.repository.permission.RoleMenuPermissionRepository;
import com.auth.api.repository.session.SessionRepository;
import com.auth.api.repository.user.RoleRepository;
import com.auth.api.repository.user.UserRepository;
import com.auth.api.repository.user.UserRoleRepository;
import com.auth.common.ApiMessages;
import com.auth.common.PasswordUtil;
import com.auth.config.Exception.ServiceException;

@Service
public class LoginServiceImpl implements ILoginService {
	private static final String RESET_PURPOSE = "PASSWORD_RESET";

	@Value("${keyStore.expiryTime}")
	private long accessTokenMinutes;

	@Value("${keyStore.path}")
	private String keyStorePath;

	@Value("${keyStore.password}")
	private String keyStorePassword;

	@Value("${keyStore.alias}")
	private String keyStoreAlias;

	@Value("${app.password-reset.expiry-minutes:15}")
	private long passwordResetExpiryMinutes;

	@Value("${app.password-reset.reset-url:http://localhost:4200/authentication/forgot-password}")
	private String passwordResetUrl;

	@Value("${spring.mail.username:marcelinoatallah6@gmail.com}")
	private String fromEmail;

	@Value("${app.security.max-failed-login-attempts:3}")
	private int maxFailedLoginAttempts;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private RoleMenuPermissionRepository roleMenuPermissionRepository;

	@Autowired
	private UmMenuRouteRepository umMenuRouteRepository;

	@Autowired
	JwtEncoder jwtEncoder;

	@Autowired
	private PasswordUtil passwordUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired(required = false)
	private JavaMailSender mailSender;

	@Autowired
	HttpServletRequest request;

	@Override
	public LoginResponse login(String username, String password) {

		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.CONFLICT));

		String decryptedPassword = "";
		try {
			decryptedPassword = passwordUtil.decryptPassword(password);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ApiMessages.PASSWORD_PROCESSING_FAILED, HttpStatus.BAD_REQUEST);
		}

		String ipAddress = request.getRemoteAddr();
		String deviceId = request.getHeader("X-DEVICE-ID");

		if (deviceId == null || deviceId.isEmpty()) {
			throw new ServiceException(ApiMessages.MISSING_DEVICE_ID, HttpStatus.BAD_REQUEST);
		}

		UserEntity secured = userRepository.findById(user.getId())
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.CONFLICT));
		if (secured.isAccountLocked()) {
			throw new ServiceException(ApiMessages.ACCOUNT_LOCKED, HttpStatus.FORBIDDEN);
		}

		Authentication authentication;
		try {
			authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(username, decryptedPassword));
		} catch (AuthenticationException ex) {
			recordFailedLoginAttempt(user.getId());
			throw new ServiceException(ApiMessages.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
		}

		List<String> roles = loadUserRoleNames(user.getId());
		requireRoleProfile(roles);
		clearFailedLoginAttempt(user.getId());

		// =========================
		// SESSION (UPSERT LOGIC)
		// =========================
		SessionEntity session = sessionRepository.findByUserIdAndDeviceId(user.getId(), deviceId)
				.orElse(new SessionEntity());

		boolean isNewSession = (session.getSessionId() == null);

		if (isNewSession) {
			session.setSessionId(UUID.randomUUID().toString());
			session.setCreatedAt(new Date());
			session.setUserId(user.getId());
			session.setDeviceId(deviceId);
			session.setActive("1");
			session.setActiveRoleName(null);
		}

		session.setIp(ipAddress);

		// refresh token rotation
		String refreshToken = UUID.randomUUID().toString();
		session.setRefreshTokenHash(BCrypt.hashpw(refreshToken, BCrypt.gensalt()));

		session.setExpiresAt(new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)));

		sessionRepository.save(session);

		SessionEntity refreshedSession = sessionRepository.findBySessionId(session.getSessionId())
				.orElse(session);

		// =========================
		// ACCESS TOKEN
		// =========================
		Instant now = Instant.now();
		Instant expiry = now.plus(accessTokenMinutes, ChronoUnit.MINUTES);

		String accessToken = generateAccessToken(user, authentication, now, expiry, ipAddress, deviceId,
				refreshedSession.getSessionId(), roles, refreshedSession);

		return buildLoginResponse(accessToken, refreshToken, refreshedSession.getSessionId(), roles,
				refreshedSession.getActiveRoleName());
	}

	@Override
	public LoginResponse refreshToken(RefreshRequest req, String deviceId, String ip) {

		SessionEntity session = sessionRepository.findBySessionId(req.getSessionId())
				.orElseThrow(() -> new ServiceException(ApiMessages.INVALID_SESSION, HttpStatus.UNAUTHORIZED));

		if (!session.isActive().equals("1")) {
			throw new ServiceException(ApiMessages.SESSION_INACTIVE, HttpStatus.UNAUTHORIZED);
		}

		if (!BCrypt.checkpw(req.getRefreshToken(), session.getRefreshTokenHash())) {
			throw new ServiceException(ApiMessages.INVALID_REFRESH_TOKEN, HttpStatus.UNAUTHORIZED);
		}

		if (!session.getDeviceId().equals(deviceId)) {
			throw new ServiceException(ApiMessages.DEVICE_MISMATCH, HttpStatus.FORBIDDEN);
		}

		if (session.getExpiresAt().before(new Date())) {
			throw new ServiceException(ApiMessages.SESSION_EXPIRED, HttpStatus.UNAUTHORIZED);
		}

		UserEntity user = userRepository.findById(session.getUserId())
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

		List<String> roles = loadUserRoleNames(user.getId());
		requireRoleProfile(roles);

		Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null);

		Instant now = Instant.now();
		Instant expiry = now.plus(accessTokenMinutes, ChronoUnit.MINUTES);

		String newAccessToken = generateAccessToken(user, auth, now, expiry, ip, deviceId, session.getSessionId(),
				roles, session);

		// rotate refresh token
		String newRefresh = UUID.randomUUID().toString();
		session.setRefreshTokenHash(BCrypt.hashpw(newRefresh, BCrypt.gensalt()));

		sessionRepository.save(session);

		return buildLoginResponse(newAccessToken, newRefresh, session.getSessionId(), roles,
				session.getActiveRoleName());
	}

	@Override
	public LoginResponse setActiveRole(ActiveRoleRequest req, String deviceId, String ip) {

		if (req == null || req.getSessionId() == null || req.getSessionId().isBlank()) {
			throw new ServiceException(ApiMessages.INVALID_SESSION, HttpStatus.BAD_REQUEST);
		}
		if (deviceId == null || deviceId.isEmpty()) {
			throw new ServiceException(ApiMessages.MISSING_DEVICE_ID, HttpStatus.BAD_REQUEST);
		}

		SessionEntity session = sessionRepository.findBySessionId(req.getSessionId())
				.orElseThrow(() -> new ServiceException(ApiMessages.INVALID_SESSION, HttpStatus.UNAUTHORIZED));

		if (!session.isActive().equals("1")) {
			throw new ServiceException(ApiMessages.SESSION_INACTIVE, HttpStatus.UNAUTHORIZED);
		}

		if (!session.getDeviceId().equals(deviceId)) {
			throw new ServiceException(ApiMessages.DEVICE_MISMATCH, HttpStatus.FORBIDDEN);
		}

		UserEntity user = userRepository.findById(session.getUserId())
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));

		List<String> roles = loadUserRoleNames(user.getId());
		requireRoleProfile(roles);

		String desired = req.getActiveRoleName();
		if (desired == null || desired.trim().isEmpty()) {
			session.setActiveRoleName(null);
		} else {
			String normalizedDesired = normalizeRoleAuthority(desired.trim());
			boolean allowed = roles.stream().map(this::normalizeRoleAuthority).anyMatch(normalizedDesired::equals);
			if (!allowed) {
				throw new ServiceException(ApiMessages.INVALID_ACTIVE_ROLE, HttpStatus.BAD_REQUEST);
			}
			session.setActiveRoleName(normalizedDesired);
		}

		sessionRepository.save(session);

		Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null);
		Instant now = Instant.now();
		Instant expiry = now.plus(accessTokenMinutes, ChronoUnit.MINUTES);

		String accessToken = generateAccessToken(user, auth, now, expiry, ip, deviceId, session.getSessionId(), roles,
				session);

		return buildLoginResponse(accessToken, null, session.getSessionId(), roles, session.getActiveRoleName());
	}

	@Override
	public void logout(String sessionId, String deviceId) {

		SessionEntity session = sessionRepository.findBySessionId(sessionId)
				.orElseThrow(() -> new ServiceException(ApiMessages.INVALID_SESSION, HttpStatus.NOT_FOUND));

		if (deviceId != null && !deviceId.equals(session.getDeviceId())) {
			throw new ServiceException(ApiMessages.DEVICE_MISMATCH, HttpStatus.FORBIDDEN);
		}

		session.setActive("0");
		session.setRefreshTokenHash(null);
		session.setSessionId(null);
		sessionRepository.save(session);
	}

	@Override
	public void forgotPassword(ForgotPasswordRequest request) {
		Optional<UserEntity> user = userRepository.findByUsername(request.getUsername().trim());
		sendPasswordResetEmail(user.orElseThrow(() -> new ServiceException("Success", HttpStatus.OK)));
	}

	@Override
	public void verifyResetToken(VerifyResetTokenRequest request) {
		Jwt jwt = decodeResetToken(extractToken(request != null ? request.getToken() : null));
		validateResetClaims(jwt);
	}

	@Override
	public void resetPassword(ResetPasswordRequest request) {

		if (request.getToken() == null || request.getToken().trim().isEmpty()) {
			throw new ServiceException(ApiMessages.INVALID_REFRESH_TOKEN, HttpStatus.BAD_REQUEST);
		}

		if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
			throw new ServiceException(ApiMessages.PASSWORD_REQUIRED, HttpStatus.BAD_REQUEST);
		}

		if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
			throw new ServiceException(ApiMessages.PASSWORD_REQUIRED, HttpStatus.BAD_REQUEST);
		}

		try {
			String newpasword = passwordUtil.decryptPassword(request.getNewPassword());
			String confirmpassword = passwordUtil.decryptPassword(request.getConfirmPassword());

			if (!newpasword.equals(confirmpassword)) {
				throw new ServiceException(ApiMessages.PASSWORDS_DO_NOT_MATCH, HttpStatus.BAD_REQUEST);
			}

			String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

			if (!newpasword.matches(passwordRegex)) {
				throw new ServiceException(ApiMessages.PASSWORD_TOO_WEAK, HttpStatus.BAD_REQUEST);
			}

			Jwt jwt = decodeResetToken(extractToken(request.getToken()));
			validateResetClaims(jwt);

			String email = jwt.getSubject();
			UserEntity user = userRepository.findByEmailIgnoreCase(email)
					.orElseThrow(() -> new ServiceException(ApiMessages.INVALID_RESET_TOKEN, HttpStatus.UNAUTHORIZED));

			user.setPassword(passwordEncoder.encode(newpasword));
			userRepository.save(user);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String extractToken(String token) {
		if (token == null || token.trim().isEmpty()) {
			throw new ServiceException("token is required", HttpStatus.BAD_REQUEST);
		}
		return token.trim();
	}

	private void sendPasswordResetEmail(UserEntity user) {
		if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
			return;
		}

		String resetToken = generatePasswordResetToken(user);
		String resetLink = passwordResetUrl + "?token=" + resetToken;

		if (mailSender == null) {
			return;
		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(user.getEmail());
		message.setSubject("Bizly password reset");
		message.setText("Hello " + user.getFirstName() + ",\n\n" + "Use this link to reset your password:\n" + resetLink
				+ "\n\n" + "This link expires in " + passwordResetExpiryMinutes + " minutes.\n\n"
				+ "If you did not request this, ignore this email.");
		mailSender.send(message);
	}

	private String generatePasswordResetToken(UserEntity user) {
		Instant now = Instant.now();
		Instant expiry = now.plus(passwordResetExpiryMinutes, ChronoUnit.MINUTES);

		JwtClaimsSet claims = JwtClaimsSet.builder().issuer("Bizly").issuedAt(now).expiresAt(expiry)
				.subject(user.getEmail()).claim("purpose", RESET_PURPOSE).claim("userId", user.getId()).build();

		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}

	private Jwt decodeResetToken(String token) {
		try {
			PublicKey publicKey = getPublicKey();
			JwtDecoder decoder = NimbusJwtDecoder.withPublicKey((java.security.interfaces.RSAPublicKey) publicKey)
					.build();
			return decoder.decode(token);
		} catch (Exception e) {
			throw new ServiceException(ApiMessages.INVALID_RESET_TOKEN, HttpStatus.UNAUTHORIZED);
		}
	}

	private void validateResetClaims(Jwt jwt) {
		String purpose = jwt.getClaimAsString("purpose");
		if (!RESET_PURPOSE.equals(purpose)) {
			throw new ServiceException(ApiMessages.INVALID_RESET_TOKEN, HttpStatus.UNAUTHORIZED);
		}
	}

	private PublicKey getPublicKey() throws Exception {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		InputStream is = getClass().getClassLoader().getResourceAsStream(keyStorePath);
		keyStore.load(is, keyStorePassword.toCharArray());
		Certificate cert = keyStore.getCertificate(keyStoreAlias);
		return cert.getPublicKey();
	}

	private List<String> loadUserRoleNames(Long userId) {
		return userRoleRepository.findRoleNamesByUserId(userId);
	}

	private void requireRoleProfile(List<String> roles) {
		if (roles == null || roles.isEmpty()) {
			throw new ServiceException(ApiMessages.NO_ROLE_PROFILE, HttpStatus.FORBIDDEN);
		}
	}

	private LoginResponse buildLoginResponse(String accessToken, String refreshToken, String sessionId,
			List<String> roles, String activeRole) {
		LoginResponse response = new LoginResponse();
		response.setToken(accessToken);
		response.setRefreshToken(refreshToken);
		response.setSessionId(sessionId);
		response.setAvailableRoles(roles);
		response.setActiveRole(activeRole);
		return response;
	}

	private void recordFailedLoginAttempt(Long userId) {
		UserEntity u = userRepository.findById(userId).orElse(null);
		if (u == null) {
			return;
		}
		int n = u.getFailedLoginAttempts() + 1;
		u.setFailedLoginAttempts(n);
		if (n >= maxFailedLoginAttempts) {
			u.setAccountLocked(true);
		}
		userRepository.save(u);
	}

	private void clearFailedLoginAttempt(Long userId) {
		UserEntity u = userRepository.findById(userId).orElse(null);
		if (u == null) {
			return;
		}
		u.setFailedLoginAttempts(0);
		userRepository.save(u);
	}

	private String normalizeRoleAuthority(String raw) {
		String trimmed = raw == null ? "" : raw.trim();
		if (trimmed.isEmpty()) {
			return "";
		}
		String upper = trimmed.toUpperCase(Locale.ROOT);
		if (upper.startsWith("ROLE_")) {
			return upper;
		}
		return "ROLE_" + upper;
	}

	private String generateAccessToken(UserEntity user, Authentication authentication, Instant now, Instant expiry,
			String ip, String deviceId, String sessionId, List<String> roles, SessionEntity session) {

		String activeRoleName = session != null ? session.getActiveRoleName() : null;

		// JwtClaimsSet rejects null claim values ("value cannot be null").
		JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder().issuer("Bizly").issuedAt(now).expiresAt(expiry)
				.subject(authentication.getName()).claim("role", roles)
				.claim("firstName", user.getFirstName() != null ? user.getFirstName() : "")
				.claim("lastName", user.getLastName() != null ? user.getLastName() : "").claim("userId", user.getId())
				.claim("deviceId", deviceId).claim("ip", ip).claim("sessionId", sessionId);
		if (activeRoleName != null && !activeRoleName.isBlank()) {
			claimsBuilder.claim("activeRole", activeRoleName);
		}
		appendPermissionClaims(claimsBuilder, resolveEffectiveRoleIdForClaims(session, roles));
		JwtClaimsSet claims = claimsBuilder.build();

		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}

	private Optional<Long> resolveEffectiveRoleIdForClaims(SessionEntity session, List<String> roles) {
		if (session != null && session.getActiveRoleName() != null && !session.getActiveRoleName().isBlank()) {
			String name = stripRolePrefixForDb(session.getActiveRoleName());
			return roleRepository.findByNameIgnoreCase(name).map(RoleEntity::getId);
		}
		if (roles == null || roles.isEmpty()) {
			return Optional.empty();
		}
		String first = stripRolePrefixForDb(roles.get(0));
		return roleRepository.findByNameIgnoreCase(first).map(RoleEntity::getId);
	}

	private static String stripRolePrefixForDb(String authority) {
		if (authority == null) {
			return "";
		}
		String t = authority.trim();
		if (t.length() > 5 && t.regionMatches(true, 0, "ROLE_", 0, 5)) {
			return t.substring(5);
		}
		return t;
	}

	private void appendPermissionClaims(JwtClaimsSet.Builder claimsBuilder, Optional<Long> roleIdOpt) {
		if (roleIdOpt.isEmpty()) {
			claimsBuilder.claim("permMatrix", false);
			claimsBuilder.claim("perms", Collections.emptyList());
			return;
		}
		Long rid = roleIdOpt.get();
		long rowCount = roleMenuPermissionRepository.countByIdRoleId(rid);
		boolean matrix = rowCount > 0;
		claimsBuilder.claim("permMatrix", matrix);
		if (!matrix) {
			claimsBuilder.claim("perms", Collections.emptyList());
			return;
		}
		List<RoleMenuPermissionEntity> rows = roleMenuPermissionRepository.findByIdRoleId(rid);
		Map<Long, String> routesByMenuId = new HashMap<>();
		if (!rows.isEmpty()) {
			List<Long> mids = rows.stream().map(r -> r.getId().getMenuId()).collect(Collectors.toList());
			for (UmMenuRouteEntity m : umMenuRouteRepository.findAllById(mids)) {
				routesByMenuId.put(m.getId(), m.getRoute() != null ? m.getRoute() : "");
			}
		}
		List<Map<String, Object>> perms = new ArrayList<>();
		for (RoleMenuPermissionEntity p : rows) {
			Map<String, Object> m = new LinkedHashMap<>();
			long menuId = p.getId().getMenuId();
			m.put("m", menuId);
			String route = routesByMenuId.get(menuId);
			if (route != null && !route.isEmpty()) {
				m.put("r", route);
			}
			m.put("v", p.isAllowView());
			m.put("a", p.isAllowAdd());
			m.put("e", p.isAllowEdit());
			m.put("d", p.isAllowDelete());
			perms.add(m);
		}
		claimsBuilder.claim("perms", perms);
	}
}