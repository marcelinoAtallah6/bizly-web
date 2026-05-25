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
import org.springframework.http.HttpMethod;
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

	/*
	 * Public paths bypass JWT validation entirely. Authenticated onboarding
	 * ({@code /auth/register-business}, {@code /auth/welcome-complete}, {@code /auth/me}, …)
	 * requires a valid JWT — the filter forwards the verified username + tenant in the
	 * X-User / X-Business-Id / X-Role-Level headers below.
	 *
	 * {@code /auth/register} is matched by equality only: a naive {@code startsWith("/auth/register")}
	 * would incorrectly treat {@code /auth/register-business} as public and skip JWT
	 * validation, leaving api-auth without X-User → 401 "Invalid session".
	 *
	 * Social login is public because the caller has not yet authenticated with Bizly
	 * (they are exchanging a provider token for a Bizly session). The same applies to
	 * /auth/business-types: the public "Create Account" page must fetch the catalog
	 * before the user has any session at all.
	 */
	/**
	 * Prefixes of paths that skip JWT; see class Javadoc for why {@code /auth/register} is not here.
	 */
	private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
		"/auth/login",
		"/auth/refresh",
		"/auth/logout",
		"/auth/business-types",
		"/auth/forgot-password",
		"/auth/forgot-password/verify",
		"/auth/forgot-password/reset",
		"/auth/social/"
	);

	private static boolean isPublicAuthPath(String path) {
		if (path == null) {
			return false;
		}
		if (path.equals("/auth/register")) {
			return true;
		}
		return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
	}

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
		return (originalExchange, chain) -> {

			String path = originalExchange.getRequest().getURI().getPath();

			boolean isPublic = isPublicAuthPath(path);

			// Defence-in-depth: clients must NEVER be able to inject our own internal trust headers.
			// Every request (public or authenticated) is stripped here; the JWT branch below re-sets
			// them from verified JWT claims. The X-Business-Override header is allowed through and
			// the downstream InternalAuthFilter only honours it when X-Role-Level == ADMIN.
			ServerHttpRequest sanitised = originalExchange.getRequest().mutate().headers(httpHeaders -> {
				httpHeaders.remove("X-User");
				httpHeaders.remove("X-Session-Id");
				httpHeaders.remove("X-Role");
				httpHeaders.remove("X-Business-Id");
				httpHeaders.remove("X-Role-Level");
				httpHeaders.remove("X-First-Login");
				httpHeaders.remove("X-Internal-Secret");
			}).build();
			final ServerWebExchange exchange = originalExchange.mutate().request(sanitised).build();

			if (isPublic) {
				return chain.filter(exchange);
			}

			/* CORS preflight: no Authorization header — must pass through so globalcors can respond */
			if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
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
				/* Prefer {@code roles} (array) — auth service issues this. Legacy tokens used singular {@code role}. */
				Object roleObj = claims.get("roles");
				if (roleObj == null) {
					roleObj = claims.get("role");
				}
				Object businessIdObj = claims.get("businessId");
				String roleLevel = claims.get("roleLevel", String.class);
				Object tenantBypassObj = claims.get("tenantBypass");
				Object firstLoginObj = claims.get("firstLogin");

				if (!tokenDeviceId.equals(deviceId)) {
					return errorResponse(exchange, "Device mismatch", HttpStatus.FORBIDDEN);
				}

				List<String> authorities = springSecurityAuthoritiesFromJwtRoleClaim(roleObj);
				final String businessIdHeader = businessIdObj == null ? "" : String.valueOf(businessIdObj);
				final String roleLevelHeader = roleLevel == null ? "" : roleLevel;
				final String firstLoginHeader = firstLoginObj == null ? "false" : String.valueOf(firstLoginObj);

				ServerHttpRequest mutated = exchange.getRequest().mutate().headers(httpHeaders -> {
					httpHeaders.set("X-User", username);
					httpHeaders.set("X-Session-Id", sessionId);
					httpHeaders.remove("X-Role");
					for (String a : authorities) {
						httpHeaders.add("X-Role", a);
					}
					// Tenant scoping headers — downstream services trust these because the JWT signature is
					// already verified upstream. Removing the inbound copy first prevents header smuggling
					// from a malicious client.
					httpHeaders.remove("X-Business-Id");
					httpHeaders.remove("X-Role-Level");
					httpHeaders.remove("X-Tenant-Bypass");
					httpHeaders.remove("X-First-Login");
					if (!businessIdHeader.isEmpty()) {
						httpHeaders.set("X-Business-Id", businessIdHeader);
					}
					if (!roleLevelHeader.isEmpty()) {
						httpHeaders.set("X-Role-Level", roleLevelHeader);
					}
					if (tenantBypassObj instanceof Boolean) {
						httpHeaders.set("X-Tenant-Bypass", ((Boolean) tenantBypassObj) ? "true" : "false");
					}
					httpHeaders.set("X-First-Login", firstLoginHeader);
					httpHeaders.set("X-Internal-Secret", "THANKSGOD_BLESSNATHALIEANDMYFAMILY_05082026");
				}).build();

				return chain.filter(exchange.mutate().request(mutated).build());

			} catch (Exception e) {
				return errorResponse(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
			}
		};
	}

	/**
	 * JWT {@code roles} claim (preferred) or legacy {@code role} claim: a list of DB role names (e.g.
	 * {@code USER}, {@code ADMIN}, or already {@code ROLE_USER}). Each is normalized to a Spring Security
	 * authority and forwarded as its own {@code X-Role} header so downstream filters can grant
	 * {@code hasRole('USER')} and {@code hasRole('ADMIN')} when the user has multiple roles.
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