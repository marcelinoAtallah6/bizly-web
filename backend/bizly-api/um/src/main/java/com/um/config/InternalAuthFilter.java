package com.um.config;

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

import com.um.config.model.session.SessionEntity;
import com.um.config.repository.SessionRepository;

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

		filterChain.doFilter(request, response);
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