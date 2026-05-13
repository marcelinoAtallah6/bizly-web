package com.settings.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settings.api.model.SettingsReportRoleGrant;
import com.settings.api.model.SettingsReportRoleGrant.GrantId;

public interface SettingsReportRoleGrantRepository extends JpaRepository<SettingsReportRoleGrant, GrantId> {

	List<SettingsReportRoleGrant> findByIdReportId(Long reportId);

	long countByIdReportId(Long reportId);

	void deleteByIdReportId(Long reportId);

	List<SettingsReportRoleGrant> findByIdRoleType(Integer roleType);
}
