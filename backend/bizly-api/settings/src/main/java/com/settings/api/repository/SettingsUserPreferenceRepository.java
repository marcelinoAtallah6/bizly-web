package com.settings.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.settings.api.model.SettingsUserPreference;

public interface SettingsUserPreferenceRepository extends JpaRepository<SettingsUserPreference, String> {

	@Query("SELECT p FROM SettingsUserPreference p WHERE LOWER(p.username) = LOWER(:username)")
	Optional<SettingsUserPreference> findByUsernameIgnoreCase(@Param("username") String username);
}
