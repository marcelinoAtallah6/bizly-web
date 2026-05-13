package com.auth.api.service.social;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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

/**
 * Social-login implementation.
 *
 * <p><strong>Verification status:</strong></p>
 * <ul>
 *   <li><strong>Google</strong> — fully implemented. Verifies the ID token against Google's
 *       {@code tokeninfo} endpoint. Replace with the {@code google-auth-library-java}
 *       client if you prefer offline JWKS validation.</li>
 *   <li><strong>Facebook</strong> — implemented via the Graph API {@code /me} call. Pass the
 *       Facebook user access token in {@code accessToken}.</li>
 *   <li><strong>Apple</strong> — skeleton only. The identity token is a signed JWT that must be
 *       validated against Apple's JWKS at {@code https://appleid.apple.com/auth/keys}. The
 *       email + sub claims are extracted but the signature is currently NOT verified —
 *       you must add Apple JWKS verification before enabling this provider in production.
 *       The method throws {@link ServiceException} until that is done.</li>
 * </ul>
 *
 * <p>For each provider, after verification we resolve the user by email; if a user with that
 * email does not exist we create one with {@code first_login = 1} so the welcome wizard
 * triggers on next page load. The user is then logged in through the standard
 * {@link ILoginService#reissueAccessTokenForUser(Long, String, String)} path so the JWT/session
 * pipeline is identical to email/password login.</p>
 */
@Service
public class SocialLoginServiceImpl implements ISocialLoginService {

	private static final Logger log = LoggerFactory.getLogger(SocialLoginServiceImpl.class);
	private static final ObjectMapper M = new ObjectMapper();

	@Value("${social.google.clientId:}")
	private String googleClientId;

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
		switch (lower) {
			case "google":   p = verifyGoogle(req); break;
			case "facebook": p = verifyFacebook(req); break;
			case "apple":    p = verifyApple(req); break;
			default:
				throw new ServiceException(ApiMessages.SOCIAL_PROVIDER_UNSUPPORTED, HttpStatus.BAD_REQUEST);
		}

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
		u.setUsername(deriveUsernameFromEmail(p.email));
		u.setFirstName(p.firstName != null ? p.firstName : "");
		u.setLastName(p.lastName != null ? p.lastName : "");
		u.setEmail(p.email);
		u.setPassword(BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt())); // disabled local password
		u.setFailedLoginAttempts(0);
		u.setAccountLocked(false);
		u.setFirstLogin(1);
		u.setAuthProvider(provider.toUpperCase(Locale.ROOT));
		u.setProviderUserId(p.providerUserId);
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
			String token = req.getIdToken();
			URL url = new URL("https://oauth2.googleapis.com/tokeninfo?id_token=" + URLEncoder.encode(token, StandardCharsets.UTF_8));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
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
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			log.warn("[SOCIAL_GOOGLE] verification failed", e);
			throw new ServiceException(ApiMessages.SOCIAL_TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
		}
	}

	private ProviderProfile verifyFacebook(SocialLoginRequest req) {
		try {
			String token = req.getAccessToken() != null ? req.getAccessToken() : req.getIdToken();
			URL url = new URL("https://graph.facebook.com/me?fields=id,email,first_name,last_name&access_token="
					+ URLEncoder.encode(token, StandardCharsets.UTF_8));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			if (conn.getResponseCode() != 200) {
				throw new ServiceException(ApiMessages.SOCIAL_TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
			}
			JsonNode body = M.readTree(readAll(conn));
			String email = textOrNull(body, "email");
			if (email == null) {
				throw new ServiceException(ApiMessages.SOCIAL_TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
			}
			ProviderProfile p = new ProviderProfile();
			p.email = email;
			p.providerUserId = textOrNull(body, "id");
			p.firstName = textOrNull(body, "first_name");
			p.lastName = textOrNull(body, "last_name");
			return p;
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			log.warn("[SOCIAL_FACEBOOK] verification failed", e);
			throw new ServiceException(ApiMessages.SOCIAL_TOKEN_INVALID, HttpStatus.UNAUTHORIZED);
		}
	}

	/**
	 * Apple identity-token verification stub. Production usage requires verifying the JWT signature
	 * against Apple's JWKS endpoint (https://appleid.apple.com/auth/keys) and checking the
	 * {@code iss}, {@code aud}, and {@code exp} claims. We refuse the request until that is wired up
	 * so a forged token cannot grant access.
	 */
	private ProviderProfile verifyApple(SocialLoginRequest req) {
		log.warn("[SECURITY_EVENT][SOCIAL_APPLE_REJECTED] reason=verifier_not_configured");
		throw new ServiceException(
				"Apple sign-in is not yet wired up — provide a JWKS verifier in SocialLoginServiceImpl.verifyApple",
				HttpStatus.NOT_IMPLEMENTED);
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

	private static String deriveUsernameFromEmail(String email) {
		int at = email.indexOf('@');
		String base = at > 0 ? email.substring(0, at) : email;
		return base.toLowerCase(Locale.ROOT) + "_" + UUID.randomUUID().toString().substring(0, 6);
	}
}
