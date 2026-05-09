package com.bizly.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class FilterConfig {

	@Bean
	public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {

		return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.authorizeExchange(ex -> ex.pathMatchers("/**").permitAll()).build();
	}
}