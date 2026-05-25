package com.um.api.service.workflow;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.um.api.model.workflow.WorkflowInstance;
import com.um.exception.ServiceException;
 
@Service
public class WorkflowHttpReplayExecutor {

	private static final Logger log = LoggerFactory.getLogger(WorkflowHttpReplayExecutor.class);
	private static final int LOG_BODY_MAX = 2000;

	@Autowired
	private RestTemplate workflowRestTemplate;
	@Autowired
	private ObjectMapper objectMapper;
	@Value("${security.internal.secret}")
	private String internalSecret;

	public void replayIfCaptured(WorkflowInstance inst) {
		if (inst.getCapturedHttpPath() == null || inst.getCapturedHttpPath().isBlank()) {
			return;
		}
		String fullPath = inst.getCapturedHttpPath().trim();
		if (!fullPath.startsWith("/")) {
			throw new ServiceException("Invalid captured path", HttpStatus.BAD_REQUEST);
		}
		int slash = fullPath.indexOf('/', 1);
		if (slash < 1) {
			throw new ServiceException("Invalid captured path — missing service segment", HttpStatus.BAD_REQUEST);
		}
		String service = fullPath.substring(1, slash).toLowerCase(Locale.ROOT);
		String downstreamPath = fullPath.substring(slash);
		StringBuilder url = new StringBuilder("http://").append(service).append(downstreamPath);
		if (inst.getCapturedHttpQuery() != null && !inst.getCapturedHttpQuery().isBlank()) {
			url.append('?').append(inst.getCapturedHttpQuery());
		}
		HttpMethod method;
		if (inst.getCapturedHttpMethod() == null || inst.getCapturedHttpMethod().isBlank()) {
			throw new ServiceException("Missing captured HTTP method", HttpStatus.BAD_REQUEST);
		}
		method = HttpMethod.resolve(inst.getCapturedHttpMethod().trim().toUpperCase(Locale.ROOT));
		if (method == null) {
			throw new ServiceException("Unsupported captured HTTP method", HttpStatus.BAD_REQUEST);
		}
		HttpHeaders headers = buildReplayHeaders(inst.getCapturedHeadersJson());
		headers.set("X-Internal-Secret", internalSecret);
		/* Tells domain services to trust X-User / X-Role without a live session row (maker session may be gone). */
		headers.set("X-Workflow-Replay", "true");
		String body = inst.getCapturedBody() == null ? "" : inst.getCapturedBody();
		HttpEntity<String> entity = new HttpEntity<>(body, headers);
		String urlStr = url.toString();
		try {
			ResponseEntity<String> resp = workflowRestTemplate.exchange(URI.create(urlStr), method, entity,
					String.class);
			if (!resp.getStatusCode().is2xxSuccessful()) {
				int code = resp.getStatusCodeValue();
				HttpStatus mapped = code >= 500 ? HttpStatus.BAD_GATEWAY : HttpStatus.FAILED_DEPENDENCY;
				String rb = resp.getBody() == null ? "" : resp.getBody();
				log.warn("Workflow replay non-2xx: method={} url={} status={} body={}", method, urlStr, code,
						truncateForLog(rb));
				throw new ServiceException("Downstream replay returned HTTP " + code + " — " + truncateForLog(rb, 4000),
						mapped);
			}
		} catch (HttpStatusCodeException ex) {
			int code = ex.getRawStatusCode();
			HttpStatus mapped = code >= 500 ? HttpStatus.BAD_GATEWAY : HttpStatus.FAILED_DEPENDENCY;
			String rb = ex.getResponseBodyAsString();
			log.warn("Workflow replay HTTP error: method={} url={} status={} body={}", method, urlStr, code,
					truncateForLog(rb), ex);
			throw new ServiceException("Downstream replay failed: HTTP " + code + " — " + rb, mapped);
		} catch (ResourceAccessException ex) {
			log.warn("Workflow replay connectivity/timeout: method={} url={}", method, urlStr, ex);
			throw new ServiceException(
					"Downstream service unreachable or timed out while replaying the approved action (check that the "
							+ "target service is up and registered in Eureka). URL: " + urlStr + " — " + ex.getMessage(),
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (ServiceException se) {
			throw se;
		} catch (Exception ex) {
			log.warn("Workflow replay failed: method={} url={}", method, urlStr, ex);
			throw new ServiceException("Downstream replay failed: " + ex.getMessage(), HttpStatus.BAD_GATEWAY);
		}
	}

	private static String truncateForLog(String s) {
		return truncateForLog(s, LOG_BODY_MAX);
	}

	private static String truncateForLog(String s, int max) {
		if (s == null) {
			return "";
		}
		if (s.length() <= max) {
			return s;
		}
		return s.substring(0, max) + "…(truncated)";
	}

	private HttpHeaders buildReplayHeaders(String capturedHeadersJson) {
		HttpHeaders out = new HttpHeaders();
		if (capturedHeadersJson == null || capturedHeadersJson.isBlank()) {
			return out;
		}
		try {
			Map<String, String> map = objectMapper.readValue(capturedHeadersJson, new TypeReference<Map<String, String>>() {
			});
			for (Map.Entry<String, String> e : map.entrySet()) {
				if (e.getKey() == null || e.getValue() == null) {
					continue;
				}
				String k = e.getKey();
				if ("X-Roles".equalsIgnoreCase(k)) {
					for (String role : splitCsv(e.getValue())) {
						out.add("X-Role", role);
					}
				} else {
					out.set(k, e.getValue());
				}
			}
		} catch (Exception e) {
			throw new ServiceException("Invalid captured headers JSON", HttpStatus.BAD_REQUEST);
		}
		return out;
	}

	private static List<String> splitCsv(String v) {
		List<String> parts = new ArrayList<>();
		for (String s : v.split(",")) {
			String t = s == null ? "" : s.trim();
			if (!t.isEmpty()) {
				parts.add(t);
			}
		}
		return parts;
	}
}
