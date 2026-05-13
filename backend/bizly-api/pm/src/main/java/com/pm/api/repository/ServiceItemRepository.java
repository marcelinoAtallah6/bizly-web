package com.pm.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pm.api.model.ServiceItem;

/**
 * Read-only access to BM's {@code bm_service_item} table from the PM module. Used by the unified
 * {@code /pm/sale/checkout} flow to resolve a service line's price and tenant. We deliberately expose
 * only tenant-scoped finders — PM must never bypass the business filter when looking up a service.
 */
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

	Optional<ServiceItem> findByIdAndBusinessId(Long id, Long businessId);
}
