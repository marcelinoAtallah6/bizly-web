package com.settings.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.settings.api.model.SettingsDashboardRoleGrant;
import com.settings.api.model.SettingsDashboardRoleGrant.GrantId;

public interface SettingsDashboardRoleGrantRepository extends JpaRepository<SettingsDashboardRoleGrant, GrantId> {

	List<SettingsDashboardRoleGrant> findByIdDashboardId(Long dashboardId);

	void deleteByIdDashboardId(Long dashboardId);

	@Query("SELECT g FROM SettingsDashboardRoleGrant g WHERE UPPER(TRIM(g.id.roleName)) = UPPER(TRIM(:roleName))")
	List<SettingsDashboardRoleGrant> findGrantsForRole(@Param("roleName") String roleName);
}
