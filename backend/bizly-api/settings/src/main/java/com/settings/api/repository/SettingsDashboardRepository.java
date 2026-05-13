package com.settings.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.settings.api.model.SettingsDashboard;

/**
 * Tenant scope: dashboards with {@code business_id IS NULL} are global (built-in / admin templates);
 * tenant-owned rows have a non-null {@code business_id}. The {@code *ForBusinessOrGlobal} variants
 * resolve both in a single query. See {@link com.settings.security.BusinessContextHolder}.
 */
public interface SettingsDashboardRepository extends JpaRepository<SettingsDashboard, Long> {

	Optional<SettingsDashboard> findBySlugIgnoreCase(String slug);

	boolean existsBySlugIgnoreCase(String slug);

	@Query("SELECT d FROM SettingsDashboard d WHERE d.id = :id "
			+ "AND (d.businessId IS NULL OR d.businessId = :businessId)")
	Optional<SettingsDashboard> findByIdForBusinessOrGlobal(@Param("id") Long id,
			@Param("businessId") Long businessId);

	@Query("SELECT d FROM SettingsDashboard d WHERE LOWER(d.slug) = LOWER(:slug) "
			+ "AND (d.businessId IS NULL OR d.businessId = :businessId)")
	Optional<SettingsDashboard> findBySlugForBusinessOrGlobal(@Param("slug") String slug,
			@Param("businessId") Long businessId);

	@Query("SELECT d FROM SettingsDashboard d WHERE LOWER(d.slug) = LOWER(:slug) "
			+ "AND d.businessId = :businessId")
	Optional<SettingsDashboard> findBySlugAndBusinessId(@Param("slug") String slug,
			@Param("businessId") Long businessId);

	@Query("SELECT d FROM SettingsDashboard d "
			+ "WHERE d.businessId IS NULL OR d.businessId = :businessId")
	List<SettingsDashboard> findAllForBusinessOrGlobal(@Param("businessId") Long businessId);
}
