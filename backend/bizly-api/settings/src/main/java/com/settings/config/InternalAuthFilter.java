package com.settings.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.settings.config.model.session.SessionEntity;
import com.settings.config.repository.SessionRepository;
import com.settings.security.BusinessContextHolder;

@Component
public class InternalAuthFilter extends OncePerRequestFilter {

	@Value("${security.internal.secret}")
	private String internalSecret;

	@Autowired
	private SessionRepository sessionRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String secret = request.getHeader("X-Internal-Secret");

		if (secret == null || !secret.equals(internalSecret)) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("application/json");
			response.getWriter().write("{\"error\":\"INVALID_INTERNAL_SECRET\"}");
			return;
		}

		if (isWorkflowReplay(request)) {
			String username = request.getHeader("X-User");
			List<GrantedAuthority> authorities = authoritiesFromGatewayRoleHeaders(request);
			if (username == null || username.isBlank() || authorities.isEmpty()) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json");
				response.getWriter().write("{\"error\":\"WORKFLOW_REPLAY_REQUIRES_USER_AND_ROLES\"}");
				return;
			}
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
					authorities);
			SecurityContextHolder.getContext().setAuthentication(auth);
			applyBusinessContextAndChain(request, response, filterChain);
			return;
		}

		String sessionId = request.getHeader("X-Session-Id");

		if (sessionId == null) {
			response.setStatus(401);
			response.getWriter().write("Missing session");
			return;
		}

		SessionEntity session = sessionRepository.findBySessionId(sessionId).orElse(null);

		if (session == null || !session.isActive().equals("1")) {
			response.setStatus(401);
			response.getWriter().write("Session inactive");
			return;
		}

		String username = request.getHeader("X-User");
		List<GrantedAuthority> authorities = resolveAuthoritiesWithSession(session,
				authoritiesFromGatewayRoleHeaders(request));

		if (username != null && !authorities.isEmpty()) {
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
					authorities);
			SecurityContextHolder.getContext().setAuthentication(auth);
		}

		applyBusinessContextAndChain(request, response, filterChain);
	}

	private void applyBusinessContextAndChain(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		Long bid = parseLong(request.getHeader("X-Business-Id"));
		String roleLevel = request.getHeader("X-Role-Level");
		Long override = null;
		if ("ADMIN".equalsIgnoreCase(roleLevel)) {
			override = parseLong(request.getHeader("X-Business-Override"));
		}
		BusinessContextHolder.set(bid, roleLevel, override, parseTenantBypass(request.getHeader("X-Tenant-Bypass")));
		try {
			filterChain.doFilter(request, response);
		} finally {
			BusinessContextHolder.clear();
		}
	}

	private static Boolean parseTenantBypass(String header) {
		if (header == null || header.isBlank()) {
			return null;
		}
		String v = header.trim();
		return "true".equalsIgnoreCase(v) || "1".equals(v);
	}

	private static boolean isWorkflowReplay(HttpServletRequest request) {
		String v = request.getHeader("X-Workflow-Replay");
		return v != null && "true".equalsIgnoreCase(v.trim());
	}

	private static Long parseLong(String s) {
		if (s == null || s.isBlank()) return null;
		try { return Long.parseLong(s.trim()); } catch (NumberFormatException e) { return null; }
	}

	/**
	 * Gateway sends one {@code X-Role} header per role when the JWT lists multiple roles. Also supports a
	 * single header value like {@code ROLE_USER,ROLE_ADMIN} for compatibility.
	 */
	private static List<GrantedAuthority> authoritiesFromGatewayRoleHeaders(HttpServletRequest request) {
		Enumeration<String> headerValues = request.getHeaders("X-Role");
		List<GrantedAuthority> authorities = new ArrayList<>();
		while (headerValues.hasMoreElements()) {
			String chunk = headerValues.nextElement();
			if (chunk == null || chunk.isBlank()) {
				continue;
			}
			for (String part : chunk.split(",")) {
				String p = part.trim();
				if (!p.isEmpty()) {
					authorities.add(new SimpleGrantedAuthority(p));
				}
			}
		}
		return authorities;
	}

	/**
	 * When {@link SessionEntity#getActiveRoleName()} is set (auth service), narrow JWT-derived authorities to that
	 * role only for authorization checks and menu filtering.
	 */
	private static List<GrantedAuthority> resolveAuthoritiesWithSession(SessionEntity session,
			List<GrantedAuthority> jwtAuthorities) {
		if (jwtAuthorities.isEmpty()) {
			return jwtAuthorities;
		}
		String active = session.getActiveRoleName();
		if (active == null || active.isBlank()) {
			return jwtAuthorities;
		}
		String trimmed = active.trim();
		for (GrantedAuthority a : jwtAuthorities) {
			if (a.getAuthority().equalsIgnoreCase(trimmed)) {
				return Collections.singletonList(new SimpleGrantedAuthority(a.getAuthority()));
			}
		}
		return jwtAuthorities;
	}
}