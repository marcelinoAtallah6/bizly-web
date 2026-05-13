package com.settings.api.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.settings.api.model.SettingsDashboard;
import com.settings.api.model.SettingsDashboardRoleGrant;
import com.settings.api.model.SettingsDashboardUserGrant;
import com.settings.api.model.UmRoleRef;
import com.settings.api.repository.SettingsDashboardRepository;
import com.settings.api.repository.SettingsDashboardRoleGrantRepository;
import com.settings.api.repository.SettingsDashboardUserGrantRepository;
import com.settings.api.repository.UmRoleRefRepository;

/**
 * Dashboard access decisions are based on {@code UM_ROLE.ROLE_TYPE} (numeric, stable) and not the
 * role name. The user's authorities arrive as role-name strings, so we translate each one to a
 * {@code role_type} via {@link UmRoleRefRepository#findFirstByNameIgnoreCaseWithType(String)}.
 */
@Service
public class DashboardAccessEvaluator {

	private static final Logger log = LoggerFactory.getLogger(DashboardAccessEvaluator.class);

	private final SettingsDashboardRepository dashboardRepository;
	private final SettingsDashboardRoleGrantRepository roleGrantRepository;
	private final SettingsDashboardUserGrantRepository userGrantRepository;
	private final UmRoleRefRepository umRoleRefRepository;

	public DashboardAccessEvaluator(SettingsDashboardRepository dashboardRepository,
			SettingsDashboardRoleGrantRepository roleGrantRepository,
			SettingsDashboardUserGrantRepository userGrantRepository,
			UmRoleRefRepository umRoleRefRepository) {
		this.dashboardRepository = dashboardRepository;
		this.roleGrantRepository = roleGrantRepository;
		this.userGrantRepository = userGrantRepository;
		this.umRoleRefRepository = umRoleRefRepository;
	}

	public boolean canAccess(String username, Collection<? extends GrantedAuthority> authorities, Long dashboardId) {
		return dashboardRepository.findById(dashboardId).map(d -> canAccess(username, authorities, d)).orElse(false);
	}

	public boolean canAccess(String username, Collection<? extends GrantedAuthority> authorities,
			SettingsDashboard dashboard) {
		long roleGrantCount = roleGrantRepository.countByIdDashboardId(dashboard.getId());
		long userGrantCount = userGrantRepository.countByIdDashboardId(dashboard.getId());

		/*
		 * No grants configured for this dashboard → treat as open to any authenticated user. Mirrors the
		 * "ungranted == public" expectation for built-in showcase dashboards and prevents lockouts after
		 * data wipes / partial seeds.
		 */
		if (roleGrantCount == 0 && userGrantCount == 0) {
			return true;
		}

		Set<Integer> userRoleTypes = currentRoleTypes(authorities);
		for (SettingsDashboardRoleGrant g : roleGrantRepository.findByIdDashboardId(dashboard.getId())) {
			if (userRoleTypes.contains(g.getId().getRoleType())) {
				return true;
			}
		}
		for (SettingsDashboardUserGrant g : userGrantRepository.findByIdDashboardId(dashboard.getId())) {
			if (g.getId().getUsername().equalsIgnoreCase(username)) {
				return true;
			}
		}

		Set<Integer> dashboardRoleTypes = roleGrantRepository.findByIdDashboardId(dashboard.getId()).stream()
				.map(g -> g.getId().getRoleType()).collect(Collectors.toSet());
		log.warn("[DASH_ACCESS_DENIED] dashboardId={} slug='{}' username='{}' userRoleTypes={} dashRoleTypes={}",
				dashboard.getId(), dashboard.getSlug(), username, userRoleTypes, dashboardRoleTypes);
		return false;
	}

	/** Maps the user's role-name authorities to the stable {@code role_type} integers via UM_ROLE. */
	public Set<Integer> currentRoleTypes(Collection<? extends GrantedAuthority> authorities) {
		Set<Integer> types = new HashSet<>();
		if (authorities == null) {
			return types;
		}
		for (GrantedAuthority a : authorities) {
			String stripped = stripRolePrefix(a.getAuthority());
			if (stripped.isEmpty()) {
				continue;
			}
			umRoleRefRepository.findFirstByNameIgnoreCaseWithType(stripped)
					.map(UmRoleRef::getRoleType)
					.ifPresent(types::add);
		}
		return types;
	}

	/** Backwards-compat helper kept for callers (e.g. SettingsDashboardManagementService) that
	 *  still need to normalize a raw role-name string to {@code ROLE_<UPPER>}. */
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

	private static String stripRolePrefix(String authority) {
		if (authority == null) {
			return "";
		}
		String t = authority.trim();
		if (t.length() > 5 && t.regionMatches(true, 0, "ROLE_", 0, 5)) {
			return t.substring(5);
		}
		return t;
	}
}
