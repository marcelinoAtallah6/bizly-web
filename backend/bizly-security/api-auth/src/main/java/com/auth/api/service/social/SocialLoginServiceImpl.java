package com.auth.api.service.social;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.api.controllers.dto.social.SocialLoginRequest;
import com.auth.api.model.login.LoginResponse;
import com.auth.api.model.session.SessionEntity;
import com.auth.api.model.user.UserEntity;
import com.auth.api.repository.session.SessionRepository;
import com.auth.api.repository.user.UserRepository;
import com.auth.api.service.login.ILoginService;
import com.auth.common.ApiMessages;
import com.auth.config.Exception.ServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SocialLoginServiceImpl implements ISocialLoginService {

	private static final Logger log = LoggerFactory.getLogger(SocialLoginServiceImpl.class);
	private static final ObjectMapper M = new ObjectMapper();

	@Value("${social.google.clientId:}")
	private String googleClientId;

	/** Outbound call to {@code oauth2.googleapis.com} — 5s was too tight on slow/VPN links. */
	@Value("${social.google.connect-timeout-ms:15000}")
	private int googleConnectTimeoutMs;

	@Value("${social.google.read-timeout-ms:30000}")
	private int googleReadTimeoutMs;

	@Autowired private UserRepository userRepository;
	@Autowired private SessionRepository sessionRepository;
	@Autowired private ILoginService loginService;

	@Override
	@Transactional
	public LoginResponse login(String provider, SocialLoginRequest req, String deviceId, String ip) {
		if (provider == null) {
			throw new ServiceException(ApiMessages.SOCIAL_PROVIDER_UNSUPPORTED, HttpStatus.BAD_REQUEST);
		}
		if (deviceId == null || deviceId.isBlank()) {
			throw new ServiceException(ApiMessages.MISSING_DEVICE_ID, HttpStatus.BAD_REQUEST);
		}
		if (req == null || (isBlank(req.getIdToken()) && isBlank(req.getAccessToken()))) {
			throw new ServiceException(ApiMessages.SOCIAL_TOKEN_INVALID, HttpStatus.BAD_REQUEST);
		}

		ProviderProfile p;
		String lower = provider.toLowerCase(Locale.ROOT);
		if (!"google".equals(lower)) {
			throw new ServiceException(ApiMessages.SOCIAL_PROVIDER_UNSUPPORTED, HttpStatus.BAD_REQUEST);
		}
		p = verifyGoogle(req);

		UserEntity user = userRepository.findByEmailIgnoreCase(p.email).orElseGet(() -> createUser(p, lower));
		// Track provider for the user (idempotent; only changes if different).
		boolean dirty = false;
		if (!lower.equalsIgnoreCase(user.getAuthProvider())) {
			user.setAuthProvider(lower.toUpperCase(Locale.ROOT));
			dirty = true;
		}
		if (p.providerUserId != null && !p.providerUserId.equals(user.getProviderUserId())) {
			user.setProviderUserId(p.providerUserId);
			dirty = true;
		}
		if (dirty) {
			userRepository.save(user);
		}

		ensureSessionForDevice(user.getId(), deviceId, ip);
		return loginService.reissueAccessTokenForUser(user.getId(), deviceId, ip);
	}

	private UserEntity createUser(ProviderProfile p, String provider) {
		UserEntity u = new UserEntity();
		u.setUsername(allocateUsernameFromProviderProfile(p, provider));
		u.setFirstName(p.firstName != null ? p.firstName : "");
		u.setLastName(p.lastName != null ? p.lastName : "");
		u.setEmail(p.email);
		u.setPassword(BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt())); // disabled local password
		u.setFailedLoginAttempts(0);
		u.setAccountLocked(false);
		// firstLogin=0 on purpose: the welcome wizard exists to make admin-issued temp passwords
		// rotate; a social user has no temp password to rotate. The SPA's OnboardingGuard will
		// detect business_id == null and funnel the user to /authentication/register-business,
		// which is the right next step for the Google flow.
		u.setFirstLogin(0);
		u.setAuthProvider(provider.toUpperCase(Locale.ROOT));
		u.setProviderUserId(p.providerUserId);
		u.setStatus("ACTIVE");
		u.setMobileNumber("-");
		return userRepository.save(u);
	}

	private void ensureSessionForDevice(Long userId, String deviceId, String ip) {
		SessionEntity session = sessionRepository.findByUserIdAndDeviceId(userId, deviceId).orElse(new SessionEntity());
		if (session.getSessionId() == null) {
			session.setSessionId(UUID.randomUUID().toString());
			session.setCreatedAt(new Date());
			session.setUserId(userId);
			session.setDeviceId(deviceId);
			session.setActive("1");
		}
		session.setIp(ip);
		String refresh = UUID.randomUUID().toString();
		session.setRefreshTokenHash(BCrypt.hashpw(refresh, BCrypt.gensalt()));
		session.setExpiresAt(new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)));
		sessionRepository.save(session);
	}

	/* --------------------------- Providers --------------------------- */

	private ProviderProfile verifyGoogle(SocialLoginRequest req) {
		try {
			for (int attempt = 0; attempt < 2; attempt++) {
				try {
					return verifyGoogleTokenInfoOnce(req.getIdToken());
				} catch (SocketTimeoutException e) {
					if (attempt == 0) {
						log.warn("[SOCIAL_GOOGLE] tokeninfo read timed out (attempt {}), retrying once — connect={}ms read={}ms",
								attempt + 1, googleConnectTimeoutMs, googleReadTimeoutMs);
						continue;
					}
					throw e;
				}
			}
			throw new IllegalStateException("unreachable");
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			log.warn("[SOCIAL_GOOGLE] verification failed", e);
			throw new ServiceException(ApiMessages.SOCIAL_TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
		}
	}

	/**
	 * Verifies the ID token against Google's tokeninfo endpoint. Timeouts are generous
	 * because {@code oauth2.googleapis.com} can exceed 5s on congested networks; a single
	 * retry on {@link SocketTimeoutException} covers transient stalls.
	 */
	private ProviderProfile verifyGoogleTokenInfoOnce(String idToken) throws Exception {
		URL url = new URL("https://oauth2.googleapis.com/tokeninfo?id_token="
				+ URLEncoder.encode(idToken, StandardCharsets.UTF_8));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setConnectTimeout(googleConnectTimeoutMs);
		conn.setReadTimeout(googleReadTimeoutMs);
		conn.setUseCaches(false);
		if (conn.getResponseCode() != 200) {
			throw new ServiceException(ApiMessages.SOCIAL_TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
		}
		JsonNode body = M.readTree(readAll(conn));
		String aud = textOrNull(body, "aud");
		String email = textOrNull(body, "email");
		String emailVerified = textOrNull(body, "email_verified");
		if (!isBlank(googleClientId) && (aud == null || !aud.equals(googleClientId))) {
			log.warn("[SECURITY_EVENT][SOCIAL_GOOGLE_REJECTED] reason=audience_mismatch aud={}", aud);
			throw new ServiceException(ApiMessages.SOCIAL_TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
		}
		if (email == null || !"true".equalsIgnoreCase(emailVerified)) {
			throw new ServiceException(ApiMessages.SOCIAL_TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
		}
		ProviderProfile p = new ProviderProfile();
		p.email = email;
		p.providerUserId = textOrNull(body, "sub");
		p.firstName = textOrNull(body, "given_name");
		p.lastName = textOrNull(body, "family_name");
		return p;
	}

	/* --------------------------- helpers --------------------------- */

	private static class ProviderProfile {
		String email;
		String providerUserId;
		String firstName;
		String lastName;
	}

	private static String readAll(HttpURLConnection conn) throws Exception {
		try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
			return r.lines().collect(Collectors.joining());
		}
	}

	private static String textOrNull(JsonNode node, String field) {
		if (node == null) return null;
		JsonNode v = node.get(field);
		return v == null || v.isNull() ? null : v.asText();
	}

	private static boolean isBlank(String s) { return s == null || s.isBlank(); }

	/**
	 * Google: username is the email local-part (sanitized); a numeric suffix is only appended if
	 * that base is already taken. Other providers keep the prior given-name + family-name rule.
	 */
	private String allocateUsernameFromProviderProfile(ProviderProfile p, String provider) {
		String base;
		if ("google".equalsIgnoreCase(provider) && !isBlank(p.email)) {
			int at = p.email.indexOf('@');
			String local = at > 0 ? p.email.substring(0, at) : p.email;
			base = lettersDigitsDotOnly(local);
		} else {
			String fn = p.firstName == null ? "" : p.firstName.trim();
			String ln = p.lastName == null ? "" : p.lastName.trim();
			String merged = (fn + ln).replaceAll("\\s+", "");
			if (merged.isEmpty()) {
				merged = "User";
			}
			base = lettersDigitsDotOnly(merged);
		}
		if (base.isBlank()) {
			base = "User";
		}
		if (base.length() < 4) {
			base = base + "User";
		}
		if (base.length() > 60) {
			base = base.substring(0, 60);
		}
		String candidate = base;
		int n = 2;
		while (userRepository.existsByUsername(candidate)) {
			String suffix = String.valueOf(n++);
			int keep = Math.max(4, 64 - suffix.length());
			candidate = base.substring(0, Math.min(base.length(), keep)) + suffix;
		}
		return candidate;
	}

	private static String lettersDigitsDotOnly(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isLetter(c) || Character.isDigit(c) || c == '.') {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
