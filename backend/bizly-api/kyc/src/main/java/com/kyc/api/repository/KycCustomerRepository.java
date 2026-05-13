package com.kyc.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kyc.api.model.customer.KycCustomer;

public interface KycCustomerRepository extends JpaRepository<KycCustomer, Long> {

	// Tenant-scoped finders. Use these from services; do not call findById directly.
	Optional<KycCustomer> findByIdAndBusinessId(Long id, Long businessId);

	Page<KycCustomer> findAllByBusinessId(Long businessId, Pageable pageable);

	@Query("SELECT c FROM KycCustomer c WHERE c.businessId = :businessId "
			+ "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :q, '%')) "
			+ "  OR LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :q, '%')) "
			+ "  OR LOWER(c.email)     LIKE LOWER(CONCAT('%', :q, '%')) "
			+ "  OR c.mobileNumber     LIKE CONCAT('%', :q, '%'))")
	Page<KycCustomer> searchByBusinessId(@Param("businessId") Long businessId, @Param("q") String q, Pageable pageable);
}
