package com.settings.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settings.api.model.SettingsNavPreference;
import com.settings.api.model.SettingsNavPreference.NavPrefId;

public interface SettingsNavPreferenceRepository extends JpaRepository<SettingsNavPreference, NavPrefId> {

	List<SettingsNavPreference> findByIdUsernameIgnoreCase(String username);
}
