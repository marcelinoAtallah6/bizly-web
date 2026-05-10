package com.settings.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settings.api.model.SettingsDashboardUserGrant;
import com.settings.api.model.SettingsDashboardUserGrant.UserGrantId;

public interface SettingsDashboardUserGrantRepository extends JpaRepository<SettingsDashboardUserGrant, UserGrantId> {

	List<SettingsDashboardUserGrant> findByIdDashboardId(Long dashboardId);

	void deleteByIdDashboardId(Long dashboardId);

	List<SettingsDashboardUserGrant> findByIdUsernameIgnoreCase(String username);
}
