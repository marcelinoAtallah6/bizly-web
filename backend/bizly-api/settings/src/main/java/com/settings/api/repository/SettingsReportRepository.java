package com.settings.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.settings.api.model.SettingsReport;

@Repository
public interface SettingsReportRepository extends JpaRepository<SettingsReport, Long> {

	Optional<SettingsReport> findByCode(String code);

	boolean existsByCodeIgnoreCase(String code);

	boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	List<SettingsReport> findByStatusOrderBySortOrderAscNameAsc(String status);

	@Query("SELECT r FROM SettingsReport r WHERE "
			+ "(:nameSearch IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :nameSearch, '%'))) "
			+ "ORDER BY r.sortOrder ASC, r.name ASC")
	Page<SettingsReport> searchByName(@Param("nameSearch") String nameSearch, Pageable pageable);

	// -- Tenant-scoped finders. NULL business_id = global / admin template, also visible. --
	@Query("SELECT r FROM SettingsReport r WHERE "
			+ "(:nameSearch IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :nameSearch, '%'))) "
			+ "AND (r.businessId IS NULL OR r.businessId = :businessId) "
			+ "ORDER BY r.sortOrder ASC, r.name ASC")
	Page<SettingsReport> searchByNameForBusinessOrGlobal(@Param("nameSearch") String nameSearch,
			@Param("businessId") Long businessId, Pageable pageable);

	@Query("SELECT r FROM SettingsReport r WHERE r.status = :status "
			+ "AND (r.businessId IS NULL OR r.businessId = :businessId) "
			+ "ORDER BY r.sortOrder ASC, r.name ASC")
	List<SettingsReport> findByStatusForBusinessOrGlobal(@Param("status") String status,
			@Param("businessId") Long businessId);

	@Query("SELECT r FROM SettingsReport r WHERE r.id = :id "
			+ "AND (r.businessId IS NULL OR r.businessId = :businessId)")
	Optional<SettingsReport> findByIdForBusinessOrGlobal(@Param("id") Long id,
			@Param("businessId") Long businessId);

	@Query("SELECT r FROM SettingsReport r WHERE LOWER(r.code) = LOWER(:code) "
			+ "AND (r.businessId IS NULL OR r.businessId = :businessId)")
	Optional<SettingsReport> findByCodeForBusinessOrGlobal(@Param("code") String code,
			@Param("businessId") Long businessId);
}
