package com.settings.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.settings.api.model.SettingsWidget;

public interface SettingsWidgetRepository extends JpaRepository<SettingsWidget, Long> {

	Optional<SettingsWidget> findByIdAndDashboard_Id(Long widgetId, Long dashboardId);

	void deleteByDashboard_Id(Long dashboardId);

	@Query("SELECT w FROM SettingsWidget w LEFT JOIN FETCH w.queryDef WHERE w.dashboard.id = :dashId ORDER BY w.sortOrder ASC")
	List<SettingsWidget> findForDashboardWithQuery(@Param("dashId") Long dashId);

	@Query("SELECT w FROM SettingsWidget w JOIN FETCH w.dashboard d LEFT JOIN FETCH w.queryDef WHERE w.id = :id")
	Optional<SettingsWidget> findWithDashboardAndQuery(@Param("id") Long id);
}
