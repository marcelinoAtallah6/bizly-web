package com.settings.api.service;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.settings.api.model.SettingsDashboard;
import com.settings.api.model.SettingsUserPreference;
import com.settings.api.repository.SettingsDashboardRepository;
import com.settings.api.repository.SettingsUserPreferenceRepository;

/**
 * Persists per-user scalar preferences. Today: the dashboard the user most recently opened, so the
 * UI can land them on it after login. Access is re-validated on read — if the user lost access to
 * the saved dashboard, the preference is silently treated as absent (and cleared on next write).
 */
@Service
public class SettingsUserPreferenceService {

	@Autowired
	private SettingsUserPreferenceRepository repository;

	@Autowired
	private SettingsDashboardRepository dashboardRepository;

	@Autowired
	private DashboardAccessEvaluator accessEvaluator;

	/**
	 * Returns the saved last-opened dashboard id for the user, but only if the dashboard still
	 * exists AND the user can still access it. Otherwise empty.
	 */
	@Transactional(readOnly = true)
	public Optional<Long> getLastDashboardId(String username, Collection<? extends GrantedAuthority> authorities) {
		if (username == null || username.isBlank()) {
			return Optional.empty();
		}
		Optional<SettingsUserPreference> pref = repository.findByUsernameIgnoreCase(username);
		if (pref.isEmpty() || pref.get().getLastDashboardId() == null) {
			return Optional.empty();
		}
		Long id = pref.get().getLastDashboardId();
		Optional<SettingsDashboard> dash = dashboardRepository.findById(id);
		if (dash.isEmpty()) {
			return Optional.empty();
		}
		if (!accessEvaluator.canAccess(username, authorities, dash.get())) {
			return Optional.empty();
		}
		return Optional.of(id);
	}

	/**
	 * Persists {@code dashboardId} as the user's last-opened dashboard. Pass {@code null} to clear.
	 * Silently ignored when the user has no access to the supplied dashboard — prevents the UI from
	 * pinning a dashboard the user shouldn't see.
	 */
	@Transactional
	public void setLastDashboardId(String username, Collection<? extends GrantedAuthority> authorities,
			Long dashboardId) {
		if (username == null || username.isBlank()) {
			return;
		}
		if (dashboardId != null) {
			Optional<SettingsDashboard> dash = dashboardRepository.findById(dashboardId);
			if (dash.isEmpty() || !accessEvaluator.canAccess(username, authorities, dash.get())) {
				return;
			}
		}
		SettingsUserPreference pref = repository.findByUsernameIgnoreCase(username)
				.orElseGet(SettingsUserPreference::new);
		if (pref.getUsername() == null) {
			pref.setUsername(username.trim());
		}
		pref.setLastDashboardId(dashboardId);
		repository.save(pref);
	}
}
