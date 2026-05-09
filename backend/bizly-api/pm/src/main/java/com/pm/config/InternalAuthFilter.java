package com.pm.config;

import java.io.IOException;
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

import com.pm.config.model.session.SessionEntity;
import com.pm.config.repository.SessionRepository;

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
        String role = request.getHeader("X-Role");

        if (username != null && role != null) {

            List<GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority(role));

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

		filterChain.doFilter(request, response);
	}
}