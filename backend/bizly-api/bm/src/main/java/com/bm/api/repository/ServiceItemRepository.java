package com.bm.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bm.api.model.ServiceItem;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {

	Page<ServiceItem> findByActiveTrue(Pageable pageable);

	// Tenant-scoped finders.
	Optional<ServiceItem> findByIdAndBusinessId(Long id, Long businessId);

	Page<ServiceItem> findByBusinessIdAndActiveTrue(Long businessId, Pageable pageable);

	Page<ServiceItem> findAllByBusinessId(Long businessId, Pageable pageable);
}
