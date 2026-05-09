package com.bizly.apigateway.security;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

			if (path.startsWith("/auth/login") || path.startsWith("/auth/refresh") || path.startsWith("/auth/logout")) {
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

				String role;

				if (roleObj instanceof List<?>) {
					role = ((List<?>) roleObj).get(0).toString();
				} else {
					role = roleObj.toString();
				}

				if (!tokenDeviceId.equals(deviceId)) {
					return errorResponse(exchange, "Device mismatch", HttpStatus.FORBIDDEN);
				}

				ServerHttpRequest mutated = exchange.getRequest().mutate().header("X-User", username)
						.header("X-Session-Id", sessionId).header("X-Role", ("ROLE_" + role).toUpperCase())
						.header("X-Internal-Secret", "THANKSGOD_BLESSNATHALIEANDMYFAMILY_05082026").build();

				return chain.filter(exchange.mutate().request(mutated).build());

			} catch (Exception e) {
				return errorResponse(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
			}
		};
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