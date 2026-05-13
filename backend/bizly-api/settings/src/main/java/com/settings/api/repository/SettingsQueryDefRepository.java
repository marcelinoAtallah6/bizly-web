package com.settings.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.settings.api.model.SettingsQueryDef;

/**
 * Tenant scope: queries with {@code business_id IS NULL} are global / admin templates and remain
 * visible to every tenant; queries owned by a business are filtered by id.
 */
public interface SettingsQueryDefRepository extends JpaRepository<SettingsQueryDef, Long> {

	boolean existsByNameIgnoreCase(String name);

	Page<SettingsQueryDef> findByNameContainingIgnoreCase(String name, Pageable pageable);

	@Query("SELECT q FROM SettingsQueryDef q "
			+ "WHERE q.businessId IS NULL OR q.businessId = :businessId")
	Page<SettingsQueryDef> findAllForBusinessOrGlobal(@Param("businessId") Long businessId, Pageable pageable);

	@Query("SELECT q FROM SettingsQueryDef q "
			+ "WHERE LOWER(q.name) LIKE LOWER(CONCAT('%', :name, '%')) "
			+ "AND (q.businessId IS NULL OR q.businessId = :businessId)")
	Page<SettingsQueryDef> findByNameForBusinessOrGlobal(@Param("name") String name,
			@Param("businessId") Long businessId, Pageable pageable);

	@Query("SELECT q FROM SettingsQueryDef q WHERE q.id = :id "
			+ "AND (q.businessId IS NULL OR q.businessId = :businessId)")
	Optional<SettingsQueryDef> findByIdForBusinessOrGlobal(@Param("id") Long id,
			@Param("businessId") Long businessId);
}
