package com.settings.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settings.api.model.SettingsDashboard;

public interface SettingsDashboardRepository extends JpaRepository<SettingsDashboard, Long> {

	Optional<SettingsDashboard> findBySlugIgnoreCase(String slug);

	boolean existsBySlugIgnoreCase(String slug);
}
