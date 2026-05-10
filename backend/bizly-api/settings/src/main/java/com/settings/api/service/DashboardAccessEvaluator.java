package com.settings.api.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.settings.api.model.SettingsDashboard;
import com.settings.api.model.SettingsDashboardRoleGrant;
import com.settings.api.model.SettingsDashboardUserGrant;
import com.settings.api.repository.SettingsDashboardRepository;
import com.settings.api.repository.SettingsDashboardRoleGrantRepository;
import com.settings.api.repository.SettingsDashboardUserGrantRepository;

@Service
public class DashboardAccessEvaluator {

	private final SettingsDashboardRepository dashboardRepository;
	private final SettingsDashboardRoleGrantRepository roleGrantRepository;
	private final SettingsDashboardUserGrantRepository userGrantRepository;

	public DashboardAccessEvaluator(SettingsDashboardRepository dashboardRepository,
			SettingsDashboardRoleGrantRepository roleGrantRepository,
			SettingsDashboardUserGrantRepository userGrantRepository) {
		this.dashboardRepository = dashboardRepository;
		this.roleGrantRepository = roleGrantRepository;
		this.userGrantRepository = userGrantRepository;
	}

	public boolean canAccess(String username, Collection<? extends GrantedAuthority> authorities, Long dashboardId) {
		return dashboardRepository.findById(dashboardId).map(d -> canAccess(username, authorities, d)).orElse(false);
	}

	public boolean canAccess(String username, Collection<? extends GrantedAuthority> authorities,
			SettingsDashboard dashboard) {
		Set<String> roleNames = new HashSet<>();
		for (GrantedAuthority a : authorities) {
			roleNames.add(normalizeRole(a.getAuthority()));
		}
		for (SettingsDashboardRoleGrant g : roleGrantRepository.findByIdDashboardId(dashboard.getId())) {
			if (roleNames.contains(normalizeRole(g.getId().getRoleName()))) {
				return true;
			}
		}
		for (SettingsDashboardUserGrant g : userGrantRepository.findByIdDashboardId(dashboard.getId())) {
			if (g.getId().getUsername().equalsIgnoreCase(username)) {
				return true;
			}
		}
		return false;
	}

	public static String normalizeRole(String authority) {
		if (authority == null) {
			return "";
		}
		String t = authority.trim();
		if (t.length() > 5 && t.regionMatches(true, 0, "ROLE_", 0, 5)) {
			return t.substring(5).toUpperCase();
		}
		return t.toUpperCase();
	}
}
