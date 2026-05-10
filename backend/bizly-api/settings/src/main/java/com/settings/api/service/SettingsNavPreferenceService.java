package com.settings.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.settings.api.model.SettingsNavPreference;
import com.settings.api.model.SettingsNavPreference.NavPrefId;
import com.settings.api.repository.SettingsNavPreferenceRepository;

@Service
public class SettingsNavPreferenceService {

	@Autowired
	private SettingsNavPreferenceRepository navPreferenceRepository;

	@Transactional
	public void setHidden(String username, Long dashboardId, boolean hiddenNav) {
		NavPrefId id = new NavPrefId();
		id.setUsername(username.trim());
		id.setDashboardId(dashboardId);
		SettingsNavPreference pref = navPreferenceRepository.findById(id).orElse(new SettingsNavPreference());
		pref.setId(id);
		pref.setHiddenNav(hiddenNav);
		navPreferenceRepository.save(pref);
	}
}
