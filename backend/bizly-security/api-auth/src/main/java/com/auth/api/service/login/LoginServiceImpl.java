package com.auth.api.service.login;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.auth.api.controllers.dto.refresh.RefreshRequest;
import com.auth.api.model.login.LoginResponse;
import com.auth.api.model.session.SessionEntity;
import com.auth.api.model.user.UserEntity;
import com.auth.api.repository.session.SessionRepository;
import com.auth.api.repository.user.UserRepository;
import com.auth.api.repository.user.UserRoleRepository;
import com.auth.common.ApiMessages;
import com.auth.common.PasswordUtil;
import com.auth.config.Exception.ServiceException;

@Service
public class LoginServiceImpl implements ILoginService {

	@Value("${keyStore.expiryTime}")
	private long accessTokenMinutes;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	JwtEncoder jwtEncoder;

	@Autowired
	private PasswordUtil passwordUtil;

	@Autowired
	HttpServletRequest request;

	@Override
	public LoginResponse login(String username, String password) {

		UserEntity user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ServiceException("User not found", HttpStatus.CONFLICT));

		String decryptedPassword = "";
		try {
			decryptedPassword = passwordUtil.decryptPassword(password);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ApiMessages.PASSWORD_PROCESSING_FAILED, HttpStatus.BAD_REQUEST);
		}

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(username, decryptedPassword));

		String ipAddress = request.getRemoteAddr();
		String deviceId = request.getHeader("X-DEVICE-ID");

		if (deviceId == null || deviceId.isEmpty()) {
			throw new ServiceException("Missing deviceId", HttpStatus.BAD_REQUEST);
		}

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
		}

		session.setIp(ipAddress);

		// refresh token rotation
		String refreshToken = UUID.randomUUID().toString();
		session.setRefreshTokenHash(BCrypt.hashpw(refreshToken, BCrypt.gensalt()));

		session.setExpiresAt(new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)));

		sessionRepository.save(session);

		// =========================
		// ACCESS TOKEN
		// =========================
		Instant now = Instant.now();
		Instant expiry = now.plus(accessTokenMinutes, ChronoUnit.MINUTES);

		List<String> roles = getUserRoles(user.getId());

		String accessToken = generateAccessToken(user, authentication, now, expiry, ipAddress, deviceId,
				session.getSessionId(), roles);

		// =========================
		// RESPONSE
		// =========================
		LoginResponse response = new LoginResponse();
		response.setToken(accessToken);
		response.setRefreshToken(refreshToken);
		response.setSessionId(session.getSessionId());

		return response;
	}

	@Override
	public LoginResponse refreshToken(RefreshRequest req, String deviceId, String ip) {

		SessionEntity session = sessionRepository.findBySessionId(req.getSessionId())
				.orElseThrow(() -> new ServiceException("Invalid session", HttpStatus.UNAUTHORIZED));

		if (!session.isActive().equals("1")) {
			throw new ServiceException("Session inactive", HttpStatus.UNAUTHORIZED);
		}

		if (!BCrypt.checkpw(req.getRefreshToken(), session.getRefreshTokenHash())) {
			throw new ServiceException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
		}

		if (!session.getDeviceId().equals(deviceId)) {
			throw new ServiceException("Device mismatch", HttpStatus.FORBIDDEN);
		}

		if (session.getExpiresAt().before(new Date())) {
			throw new ServiceException("Session expired", HttpStatus.UNAUTHORIZED);
		}

		UserEntity user = userRepository.findById(session.getUserId())
				.orElseThrow(() -> new ServiceException("User not found", HttpStatus.NOT_FOUND));

		Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null);

		Instant now = Instant.now();
		Instant expiry = now.plus(accessTokenMinutes, ChronoUnit.MINUTES);

		List<String> roles = getUserRoles(user.getId());

		String newAccessToken = generateAccessToken(user, auth, now, expiry, ip, deviceId, session.getSessionId(),
				roles);

		// rotate refresh token
		String newRefresh = UUID.randomUUID().toString();
		session.setRefreshTokenHash(BCrypt.hashpw(newRefresh, BCrypt.gensalt()));

		sessionRepository.save(session);

		LoginResponse response = new LoginResponse();
		response.setToken(newAccessToken);
		response.setRefreshToken(newRefresh);
		response.setSessionId(session.getSessionId());

		return response;
	}

	@Override
	public void logout(String sessionId, String deviceId) {

		SessionEntity session = sessionRepository.findBySessionId(sessionId)
				.orElseThrow(() -> new ServiceException("Session not found", HttpStatus.NOT_FOUND));

		if (deviceId != null && !deviceId.equals(session.getDeviceId())) {
			throw new ServiceException("Device mismatch", HttpStatus.FORBIDDEN);
		}

		session.setActive("0");
		session.setRefreshTokenHash(null);
		session.setSessionId(null);
		sessionRepository.save(session);
	}

	public List<String> getUserRoles(Long userId) {
		return userRoleRepository.findByIdUserId(userId).stream().map(ur -> ur.getRole().getName())
				.collect(Collectors.toList());
	}

	// ======================
	// TOKEN GENERATION CLEAN
	// ======================
	private String generateAccessToken(UserEntity user, Authentication authentication, Instant now, Instant expiry,
			String ip, String deviceId, String sessionId, List<String> roles) {

		JwtClaimsSet claims = JwtClaimsSet.builder().issuer("Bizly").issuedAt(now).expiresAt(expiry)
				.subject(authentication.getName()).claim("role", roles).claim("firstName", user.getFirstName())
				.claim("lastName", user.getLastName()).claim("userId", user.getId()).claim("deviceId", deviceId)
				.claim("ip", ip).claim("sessionId", sessionId).build();

		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}
}