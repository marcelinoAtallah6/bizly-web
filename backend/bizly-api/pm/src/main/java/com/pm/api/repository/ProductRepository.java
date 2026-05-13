package com.pm.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.pm.api.model.Product;

/**
 * Tenant-scoped finders. Use these from services; never call the inherited
 * {@code findById} / {@code findAll} directly — they bypass {@code business_id}.
 * See {@link com.pm.security.BusinessContextHolder}.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findByIdAndBusinessId(Long id, Long businessId);

	Page<Product> findAllByBusinessId(Long businessId, Pageable pageable);
}
