package com.settings.api.controller;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

final class SettingsSecuritySupport {

	private SettingsSecuritySupport() {
	}

	static String currentUsername() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null ? auth.getName() : "";
	}

	static Collection<? extends GrantedAuthority> currentAuthorities() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return Collections.emptyList();
		}
		return auth.getAuthorities();
	}
}
