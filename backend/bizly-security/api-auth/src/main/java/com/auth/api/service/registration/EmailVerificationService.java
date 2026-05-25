package com.auth.api.service.registration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;

import com.auth.api.model.user.UserEntity;
import com.auth.api.repository.user.UserRepository;
import com.auth.common.ApiMessages;
import com.auth.common.PasswordUtil;
import com.auth.config.Exception.ServiceException;

@Service
public class EmailVerificationService {

	public static final String PURPOSE_VERIFY_SET_PASSWORD = "VERIFY_EMAIL_SET_PASSWORD";

	private static final Pattern STRONG_PWD = Pattern
			.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

	@Autowired private UserRepository userRepository;
	@Autowired private PasswordEncoder passwordEncoder;
	@Autowired private PasswordUtil passwordUtil;
	@Autowired private JwtEncoder jwtEncoder;
	@Autowired(required = false) private JavaMailSender mailSender;

	@Value("${app.registration.verify-url:http://localhost:4200/authentication/verify-email}")
	private String verifyUrl;

	@Value("${app.registration.verify-expiry-minutes:1440}")
	private int verifyExpiryMinutes;

	@Value("${spring.mail.username:}")
	private String fromEmail;

	@Value("${keyStore.path}")
	private String keyStorePath;

	@Value("${keyStore.password}")
	private String keyStorePassword;

	@Value("${keyStore.alias}")
	private String keyStoreAlias;

	public void sendOwnerInvite(UserEntity user, String businessName) {
		if (user.getEmail() == null || user.getEmail().isBlank() || mailSender == null) {
			return;
		}
		String token = issueToken(user.getId(), user.getEmail());
		String link = verifyUrl + "?token=" + token;
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom(fromEmail);
		msg.setTo(user.getEmail());
		msg.setSubject("Your Bizly business account is ready");
		msg.setText("Hello " + user.getFirstName() + ",\n\n"
				+ "Your account for " + businessName + " has been created.\n\n"
				+ "Username: " + user.getUsername() + "\n"
				+ "Email: " + user.getEmail() + "\n\n"
				+ "Click the link below to verify your email and set your password:\n"
				+ link + "\n\n"
				+ "This link expires in " + verifyExpiryMinutes + " minutes.\n\n"
				+ "— Bizly Team");
		mailSender.send(msg);
	}

	public String issueToken(long userId, String email) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("Bizly")
				.issuedAt(now)
				.expiresAt(now.plus(verifyExpiryMinutes, ChronoUnit.MINUTES))
				.subject(email)
				.claim("purpose", PURPOSE_VERIFY_SET_PASSWORD)
				.claim("userId", userId)
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}

	@Transactional
	public void verifyAndSetPassword(String token, String encryptedPassword) {
		Jwt jwt = decode(token);
		if (!PURPOSE_VERIFY_SET_PASSWORD.equals(jwt.getClaimAsString("purpose"))) {
			throw new ServiceException(ApiMessages.INVALID_RESET_TOKEN, HttpStatus.UNAUTHORIZED);
		}
		Long userId = jwt.getClaim("userId");
		if (userId == null) {
			throw new ServiceException(ApiMessages.INVALID_RESET_TOKEN, HttpStatus.UNAUTHORIZED);
		}
		UserEntity user = userRepository.findById(userId.longValue())
				.orElseThrow(() -> new ServiceException(ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
		String plain;
		try {
			plain = passwordUtil.decryptPassword(encryptedPassword);
		} catch (Exception e) {
			throw new ServiceException(ApiMessages.PASSWORD_PROCESSING_FAILED, HttpStatus.BAD_REQUEST);
		}
		if (!STRONG_PWD.matcher(plain).matches()) {
			throw new ServiceException(ApiMessages.PASSWORD_TOO_WEAK, HttpStatus.BAD_REQUEST);
		}
		user.setPassword(passwordEncoder.encode(plain));
		user.setStatus("ACTIVE");
		user.setEmailVerifiedAt(LocalDateTime.now());
		user.setExpiresAt(null);
		user.setFirstLogin(1);
		userRepository.save(user);
	}

	private Jwt decode(String token) {
		try {
			PublicKey publicKey = loadPublicKey();
			JwtDecoder decoder = NimbusJwtDecoder.withPublicKey((java.security.interfaces.RSAPublicKey) publicKey)
					.build();
			return decoder.decode(token.trim());
		} catch (Exception e) {
			throw new ServiceException(ApiMessages.INVALID_RESET_TOKEN, HttpStatus.UNAUTHORIZED);
		}
	}

	private PublicKey loadPublicKey() throws Exception {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(keyStorePath)) {
			keyStore.load(is, keyStorePassword.toCharArray());
		}
		Certificate cert = keyStore.getCertificate(keyStoreAlias);
		return cert.getPublicKey();
	}
}
