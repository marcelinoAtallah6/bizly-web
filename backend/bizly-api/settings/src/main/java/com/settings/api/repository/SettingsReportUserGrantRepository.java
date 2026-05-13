package com.settings.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settings.api.model.SettingsReportUserGrant;
import com.settings.api.model.SettingsReportUserGrant.UserGrantId;

public interface SettingsReportUserGrantRepository extends JpaRepository<SettingsReportUserGrant, UserGrantId> {

	List<SettingsReportUserGrant> findByIdReportId(Long reportId);

	long countByIdReportId(Long reportId);

	void deleteByIdReportId(Long reportId);

	List<SettingsReportUserGrant> findByIdUsernameIgnoreCase(String username);
}
