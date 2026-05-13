package com.settings.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settings.api.model.SettingsQueryDefRoleGrant;
import com.settings.api.model.SettingsQueryDefRoleGrant.GrantId;

public interface SettingsQueryDefRoleGrantRepository extends JpaRepository<SettingsQueryDefRoleGrant, GrantId> {

	List<SettingsQueryDefRoleGrant> findByIdQueryDefId(Long queryDefId);

	long countByIdQueryDefId(Long queryDefId);

	void deleteByIdQueryDefId(Long queryDefId);

	List<SettingsQueryDefRoleGrant> findByIdRoleType(Integer roleType);
}
