package com.settings.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settings.api.model.SettingsDashboardRoleGrant;
import com.settings.api.model.SettingsDashboardRoleGrant.GrantId;

public interface SettingsDashboardRoleGrantRepository extends JpaRepository<SettingsDashboardRoleGrant, GrantId> {

	List<SettingsDashboardRoleGrant> findByIdDashboardId(Long dashboardId);

	long countByIdDashboardId(Long dashboardId);

	void deleteByIdDashboardId(Long dashboardId);

	List<SettingsDashboardRoleGrant> findByIdRoleType(Integer roleType);
}
