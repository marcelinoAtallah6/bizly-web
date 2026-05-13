package com.pm.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pm.api.model.CustomerSale;

/**
 * Tenant-scoped finders. Use these from services; never call the inherited
 * {@code findById} / {@code findAll} directly — they bypass {@code business_id}.
 * See {@link com.pm.security.BusinessContextHolder}.
 */
@Repository
public interface CustomerSaleRepository extends JpaRepository<CustomerSale, Long> {

	Optional<CustomerSale> findByIdAndBusinessId(Long id, Long businessId);

	Page<CustomerSale> findAllByBusinessId(Long businessId, Pageable pageable);

	Page<CustomerSale> findByBusinessIdAndCustomerIdOrderByCreatedAtDesc(Long businessId, Long customerId, Pageable pageable);
}
