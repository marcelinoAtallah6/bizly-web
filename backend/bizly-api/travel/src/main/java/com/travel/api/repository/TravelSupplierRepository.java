package com.travel.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.api.model.TravelSupplier;

public interface TravelSupplierRepository extends JpaRepository<TravelSupplier, Long> {

	Optional<TravelSupplier> findByIdAndBusinessId(Long id, Long businessId);

	Page<TravelSupplier> findAllByBusinessId(Long businessId, Pageable pageable);
}
