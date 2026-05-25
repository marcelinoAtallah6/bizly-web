package com.auth.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		/*
		 * api-auth sits behind the Spring Cloud Gateway. The gateway is the only place that
		 * validates inbound JWTs and forwards X-User / X-Session-Id / X-Role headers.
		 *
		 * Every business-registration endpoint is exposed permitAll() here because we trust
		 * the gateway. Each controller still rejects requests that arrive without an X-User
		 * header (= bypassing the gateway) — see RegisterBusinessController#requireUser.
		 *
		 * Social-login endpoints are permitAll because the caller does NOT yet have a Bizly
		 * JWT — the controller exchanges a provider token for one.
		 */
		http.csrf().disable().authorizeRequests()
				.antMatchers("/auth/login", "/auth/refresh", "/auth/logout", "/auth/session/active-role",
						"/auth/forgot-password/**",
						"/auth/register", "/auth/register-business", "/auth/welcome-complete", "/auth/me",
						"/auth/me/avatar",
						"/auth/roles/assignable",
						"/auth/business-types",
						"/auth/social/**",
						"/auth/admin/**",
						"/auth/platform-config",
						"/auth/verify-email/**",
						"/auth/webhooks/**")
				.permitAll().anyRequest().authenticated().and().formLogin().disable();

		return http.build();
	}

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}