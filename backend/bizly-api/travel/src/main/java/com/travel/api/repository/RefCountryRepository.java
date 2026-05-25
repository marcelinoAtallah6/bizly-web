package com.travel.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.travel.api.model.RefCountry;

public interface RefCountryRepository extends JpaRepository<RefCountry, Long> {

	@Query("SELECT c FROM RefCountry c WHERE c.active = 1 AND (:q IS NULL OR :q = '' OR "
			+ "LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.iso2) LIKE LOWER(CONCAT('%', :q, '%')) OR "
			+ "LOWER(c.iso3) LIKE LOWER(CONCAT('%', :q, '%'))) ORDER BY c.name")
	Page<RefCountry> searchActive(@Param("q") String query, Pageable pageable);
}
