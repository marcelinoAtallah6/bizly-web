package com.bizly.apigateway.security;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationPreFilter extends AbstractGatewayFilterFactory<AuthenticationPreFilter.Config> {

	private static final List<String> PUBLIC_PATHS = List.of(
		"/auth/login",
		"/auth/refresh",
		"/auth/logout",
		"/auth/forgot-password",
		"/auth/forgot-password/verify",
		"/auth/forgot-password/reset"
	);

	@Value("${keyStore.path}")
	private String keyStorePath;

	@Value("${keyStore.password}")
	private String keyStorePassword;

	@Value("${keyStore.alias}")
	private String keyStoreAlias;

	private final ObjectMapper objectMapper;

	public AuthenticationPreFilter(ObjectMapper objectMapper) {
		super(Config.class);
		this.objectMapper = objectMapper;
	}

	public static class Config {
	}

	private PublicKey getPublicKey() throws Exception {
		KeyStore keyStore = KeyStore.getInstance("JKS");
		InputStream is = getClass().getClassLoader().getResourceAsStream(keyStorePath);
		keyStore.load(is, keyStorePassword.toCharArray());
		Certificate cert = keyStore.getCertificate(keyStoreAlias);
		return cert.getPublicKey();
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {

			String path = exchange.getRequest().getURI().getPath();

			boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);

			if (isPublic) {
				return chain.filter(exchange);
			}
			
			HttpHeaders headers = exchange.getRequest().getHeaders();
			String token = headers.getFirst(HttpHeaders.AUTHORIZATION);
			String deviceId = headers.getFirst("X-DEVICE-ID");

			if (token == null || !token.startsWith("Bearer ")) {
				return errorResponse(exchange, "Missing token", HttpStatus.UNAUTHORIZED);
			}

			token = token.substring(7);

			try {
				PublicKey publicKey = getPublicKey();

				Claims claims = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();

				String username = claims.getSubject();
				String sessionId = claims.get("sessionId", String.class);
				String tokenDeviceId = claims.get("deviceId", String.class);
				Object roleObj = claims.get("role");

				if (!tokenDeviceId.equals(deviceId)) {
					return errorResponse(exchange, "Device mismatch", HttpStatus.FORBIDDEN);
				}

				List<String> authorities = springSecurityAuthoritiesFromJwtRoleClaim(roleObj);

				ServerHttpRequest mutated = exchange.getRequest().mutate().headers(httpHeaders -> {
					httpHeaders.set("X-User", username);
					httpHeaders.set("X-Session-Id", sessionId);
					httpHeaders.remove("X-Role");
					for (String a : authorities) {
						httpHeaders.add("X-Role", a);
					}
					httpHeaders.set("X-Internal-Secret", "THANKSGOD_BLESSNATHALIEANDMYFAMILY_05082026");
				}).build();

				return chain.filter(exchange.mutate().request(mutated).build());

			} catch (Exception e) {
				return errorResponse(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
			}
		};
	}

	/**
	 * JWT {@code role} claim is a list of DB role names (e.g. {@code USER}, {@code ADMIN}, or already
	 * {@code ROLE_USER}). Each is normalized to a Spring Security authority and forwarded as its own
	 * {@code X-Role} header so downstream filters can grant {@code hasRole('USER')} and {@code hasRole('ADMIN')}
	 * when the user has multiple roles.
	 */
	private static List<String> springSecurityAuthoritiesFromJwtRoleClaim(Object roleObj) {
		if (roleObj == null) {
			return List.of("ROLE_USER");
		}
		if (roleObj instanceof List<?>) {
			List<?> list = (List<?>) roleObj;
			if (list.isEmpty()) {
				return List.of("ROLE_USER");
			}
			List<String> out = new ArrayList<>();
			for (Object o : list) {
				out.add(normalizeAuthority(String.valueOf(o)));
			}
			return List.copyOf(out);
		}
		return List.of(normalizeAuthority(roleObj.toString()));
	}

	private static String normalizeAuthority(String raw) {
		String trimmed = raw == null ? "" : raw.trim();
		if (trimmed.isEmpty()) {
			return "ROLE_USER";
		}
		String upper = trimmed.toUpperCase(Locale.ROOT);
		if (upper.startsWith("ROLE_")) {
			return upper;
		}
		return "ROLE_" + upper;
	}

	private Mono<Void> errorResponse(ServerWebExchange exchange, String msg, HttpStatus status) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(status);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> body = Map.of("timestamp",
				ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), "error", msg, "status",
				status.value());

		try {
			byte[] bytes = objectMapper.writeValueAsBytes(body);
			return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
		} catch (Exception ex) {
			return response.setComplete();
		}
	}

}