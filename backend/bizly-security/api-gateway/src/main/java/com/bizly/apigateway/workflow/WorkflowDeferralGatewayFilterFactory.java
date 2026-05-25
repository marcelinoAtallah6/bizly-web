package com.bizly.apigateway.workflow;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

/**
 * Optional gateway filter: matches mutating traffic against UM workflow rules; on hit, captures the
 * request into {@code UM_WORKFLOW_INSTANCE} and returns HTTP 202 instead of forwarding to the domain service.
 */
@Component
public class WorkflowDeferralGatewayFilterFactory
		extends AbstractGatewayFilterFactory<WorkflowDeferralGatewayFilterFactory.Config> {

	private final WebClient webClient;
	private final ObjectMapper objectMapper;
	private final BizlyWorkflowGatewayProperties props;

	public WorkflowDeferralGatewayFilterFactory(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
			BizlyWorkflowGatewayProperties props) {
		super(Config.class);
		this.webClient = webClientBuilder.build();
		this.objectMapper = objectMapper;
		this.props = props;
	}

	public static class Config {
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			if (!props.isDeferralEnabled()) {
				return chain.filter(exchange);
			}
			ServerHttpRequest req = exchange.getRequest();
			HttpMethod m = req.getMethod();
			if (m == null || m == HttpMethod.GET || m == HttpMethod.HEAD || m == HttpMethod.OPTIONS) {
				return chain.filter(exchange);
			}
			String path = req.getURI().getPath();
			if (path.startsWith("/auth/") || path.startsWith("/um/workflow")) {
				return chain.filter(exchange);
			}
			if (!path.matches("/(bm|pm|kyc|settings|broadcast)/.*")) {
				return chain.filter(exchange);
			}
			return matchAndMaybeDefer(exchange, chain, path, m.name());
		};
	}

	private Mono<Void> matchAndMaybeDefer(ServerWebExchange exchange, GatewayFilterChain chain, String path,
			String method) {
		Map<String, Object> matchBody = new LinkedHashMap<>();
		matchBody.put("path", path);
		matchBody.put("method", method);
		String bid = exchange.getRequest().getHeaders().getFirst("X-Business-Id");
		if (bid != null && !bid.isBlank()) {
			try {
				matchBody.put("businessId", Long.parseLong(bid));
			} catch (@SuppressWarnings("unused") NumberFormatException ignore) {
				// omit business id
			}
		}
		return webClient.post().uri("http://um/workflow/gateway/match").headers(h -> copyTrustHeaders(exchange, h))
				.contentType(MediaType.APPLICATION_JSON).bodyValue(matchBody).retrieve().bodyToMono(String.class)
				.flatMap(json -> {
					try {
						JsonNode root = objectMapper.readTree(json);
						if (!root.path("success").asBoolean(false)) {
							return chain.filter(exchange);
						}
						JsonNode data = root.get("data");
						if (data == null || data.isNull()) {
							return chain.filter(exchange);
						}
						String reason = data.path("reason").asText("");
						if ("maker_denied".equals(reason)) {
							exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
							exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
							Map<String, Object> err = new LinkedHashMap<>();
							err.put("success", Boolean.FALSE);
							err.put("message",
									"This action is limited to the maker roles or users configured for this workflow.");
							byte[] errBytes = objectMapper.writeValueAsBytes(err);
							return exchange.getResponse()
									.writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(errBytes)));
						}
						if (!data.path("defer").asBoolean(false)) {
							if ("endpoint_matched".equals(reason)) {
								long endpointId = data.path("endpointId").asLong();
								return chain.filter(exchange)
										.then(Mono.defer(() -> notifyEngineOnSuccess(exchange, endpointId)));
							}
							return chain.filter(exchange);
						}
						long endpointId = data.path("endpointId").asLong();
						return readRequestBody(exchange)
								.flatMap(bytes -> capture(exchange, chain, path, method, endpointId, bytes));
					} catch (Exception e) {
						return chain.filter(exchange);
					}
				}).onErrorResume(ex -> chain.filter(exchange));
	}

	private Mono<byte[]> readRequestBody(ServerWebExchange exchange) {
		return DataBufferUtils.join(exchange.getRequest().getBody())
				.defaultIfEmpty(exchange.getResponse().bufferFactory().allocateBuffer(0)).map(dataBuffer -> {
					byte[] b = new byte[dataBuffer.readableByteCount()];
					dataBuffer.read(b);
					DataBufferUtils.release(dataBuffer);
					return b;
				});
	}

	private Mono<Void> capture(ServerWebExchange exchange, GatewayFilterChain chain, String path, String method,
			long endpointId, byte[] rawBody) {
		Map<String, Object> cap = new LinkedHashMap<>();
		cap.put("endpointId", endpointId);
		cap.put("path", path);
		cap.put("method", method);
		String q = exchange.getRequest().getURI().getRawQuery();
		if (q != null && !q.isBlank()) {
			cap.put("query", q);
		}
		cap.put("headers", trustHeadersForCapture(exchange.getRequest()));
		cap.put("body", new String(rawBody, StandardCharsets.UTF_8));
		String bid = exchange.getRequest().getHeaders().getFirst("X-Business-Id");
		if (bid != null && !bid.isBlank()) {
			try {
				cap.put("businessId", Long.parseLong(bid));
			} catch (@SuppressWarnings("unused") NumberFormatException ignore) {
				// omit
			}
		}
		return webClient.post().uri("http://um/workflow/gateway/capture").headers(h -> copyTrustHeaders(exchange, h))
				.contentType(MediaType.APPLICATION_JSON).bodyValue(cap).exchangeToMono(response -> {
					if (response.statusCode() == HttpStatus.ACCEPTED) {
						return response.bodyToMono(byte[].class).defaultIfEmpty(new byte[0]).flatMap(bytes -> {
							exchange.getResponse().setStatusCode(HttpStatus.ACCEPTED);
							exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
							return exchange.getResponse()
									.writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
						});
					}
					return response.bodyToMono(byte[].class).defaultIfEmpty(new byte[0]).flatMap(errBytes -> {
						exchange.getResponse().setStatusCode(response.statusCode());
						exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
						return exchange.getResponse()
								.writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(errBytes)));
					});
				}).onErrorResume(ex -> writeGatewayError(exchange,
						"Workflow capture failed; the action was not submitted for approval."));
	}

	private Mono<Void> writeGatewayError(ServerWebExchange exchange, String message) {
		try {
			Map<String, Object> err = new LinkedHashMap<>();
			err.put("success", Boolean.FALSE);
			err.put("message", message);
			byte[] bytes = objectMapper.writeValueAsBytes(err);
			exchange.getResponse().setStatusCode(HttpStatus.BAD_GATEWAY);
			exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
			return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
		} catch (Exception e) {
			exchange.getResponse().setStatusCode(HttpStatus.BAD_GATEWAY);
			return exchange.getResponse().setComplete();
		}
	}

	private static void copyTrustHeaders(ServerWebExchange exchange, HttpHeaders target) {
		HttpHeaders src = exchange.getRequest().getHeaders();
		copyIfPresent(target, src, "X-Internal-Secret");
		copyIfPresent(target, src, "X-Session-Id");
		copyIfPresent(target, src, "X-User");
		copyIfPresent(target, src, "X-Business-Id");
		copyIfPresent(target, src, "X-Role-Level");
		copyIfPresent(target, src, "X-First-Login");
		List<String> roles = src.getOrEmpty("X-Role");
		for (String r : roles) {
			target.add("X-Role", r);
		}
	}

	private static void copyIfPresent(HttpHeaders target, HttpHeaders src, String name) {
		String v = src.getFirst(name);
		if (v != null && !v.isBlank()) {
			target.set(name, v);
		}
	}

	private static Map<String, String> trustHeadersForCapture(ServerHttpRequest req) {
		Map<String, String> m = new LinkedHashMap<>();
		HttpHeaders h = req.getHeaders();
		putIfPresent(m, h, "X-User");
		putIfPresent(m, h, "X-Session-Id");
		putIfPresent(m, h, "X-Business-Id");
		putIfPresent(m, h, "X-Role-Level");
		putIfPresent(m, h, "X-First-Login");
		putIfPresent(m, h, "Content-Type");
		putIfPresent(m, h, "Accept");
		List<String> roles = h.getOrEmpty("X-Role");
		if (!roles.isEmpty()) {
			m.put("X-Roles", String.join(",", roles));
		}
		return m;
	}

	private static void putIfPresent(Map<String, String> m, HttpHeaders h, String name) {
		String v = h.getFirst(name);
		if (v != null && !v.isBlank()) {
			m.put(name, v);
		}
	}

	private Mono<Void> notifyEngineOnSuccess(ServerWebExchange exchange, long endpointId) {
		HttpStatus status = exchange.getResponse().getStatusCode();
		if (status != null && status.series() != HttpStatus.Series.SUCCESSFUL) {
			org.slf4j.LoggerFactory.getLogger(WorkflowDeferralGatewayFilterFactory.class)
					.debug("[WF_GATEWAY] skip engine notify endpointId={} httpStatus={}", endpointId, status);
			return Mono.empty();
		}
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("endpointId", endpointId);
		String bid = exchange.getRequest().getHeaders().getFirst("X-Business-Id");
		if (bid != null && !bid.isBlank()) {
			try {
				body.put("businessId", Long.parseLong(bid));
			} catch (@SuppressWarnings("unused") NumberFormatException ignore) {
				// omit
			}
		}
		Map<String, Object> ctx = new LinkedHashMap<>();
		String actor = exchange.getRequest().getHeaders().getFirst("X-User");
		if (actor != null && !actor.isBlank()) {
			ctx.put("username", actor.trim());
		}
		body.put("context", ctx);
		return webClient.post().uri("http://um/workflow/gateway/notify-completed")
				.headers(h -> copyTrustHeaders(exchange, h)).contentType(MediaType.APPLICATION_JSON).bodyValue(body)
				.retrieve().bodyToMono(Void.class)
				.doOnSuccess(v -> org.slf4j.LoggerFactory.getLogger(WorkflowDeferralGatewayFilterFactory.class)
						.info("[WF_GATEWAY] engine notify OK endpointId={}", endpointId))
				.onErrorResume(ex -> {
					org.slf4j.LoggerFactory.getLogger(WorkflowDeferralGatewayFilterFactory.class)
							.warn("[WF_GATEWAY] notify-completed failed endpointId={}: {}", endpointId,
									ex.getMessage());
					return Mono.empty();
				}).then();
	}
}
