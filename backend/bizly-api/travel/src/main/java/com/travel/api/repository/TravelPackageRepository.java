package com.travel.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.api.model.TravelPackage;

public interface TravelPackageRepository extends JpaRepository<TravelPackage, Long> {

	Optional<TravelPackage> findByIdAndBusinessId(Long id, Long businessId);

	Page<TravelPackage> findAllByBusinessId(Long businessId, Pageable pageable);

	long countByBusinessId(Long businessId);
}
