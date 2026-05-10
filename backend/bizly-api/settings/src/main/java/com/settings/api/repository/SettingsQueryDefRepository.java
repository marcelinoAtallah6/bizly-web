package com.settings.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.settings.api.model.SettingsQueryDef;

public interface SettingsQueryDefRepository extends JpaRepository<SettingsQueryDef, Long> {

	boolean existsByNameIgnoreCase(String name);

	Page<SettingsQueryDef> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
