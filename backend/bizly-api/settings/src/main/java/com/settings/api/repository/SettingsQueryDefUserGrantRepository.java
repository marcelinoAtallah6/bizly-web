package com.settings.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settings.api.model.SettingsQueryDefUserGrant;
import com.settings.api.model.SettingsQueryDefUserGrant.UserGrantId;

public interface SettingsQueryDefUserGrantRepository extends JpaRepository<SettingsQueryDefUserGrant, UserGrantId> {

	List<SettingsQueryDefUserGrant> findByIdQueryDefId(Long queryDefId);

	long countByIdQueryDefId(Long queryDefId);

	void deleteByIdQueryDefId(Long queryDefId);

	List<SettingsQueryDefUserGrant> findByIdUsernameIgnoreCase(String username);
}
