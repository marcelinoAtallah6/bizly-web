package com.auth.api.repository.business;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.auth.api.model.business.BusinessEntity;

@Repository
public interface BusinessRepository extends JpaRepository<BusinessEntity, Long> {
	Optional<BusinessEntity> findByBusinessNameIgnoreCase(String businessName);
	boolean existsByBusinessNameIgnoreCase(String businessName);

	/** Admin context-switcher autocomplete. Matches name OR exact numeric id. */
	@Query("SELECT b FROM BusinessEntity b "
			+ "WHERE LOWER(b.businessName) LIKE LOWER(CONCAT('%', :q, '%')) "
			+ "ORDER BY b.businessName ASC")
	List<BusinessEntity> searchByName(@Param("q") String q, Pageable pageable);
}
