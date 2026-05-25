package com.travel.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.api.model.TravelDestination;

public interface TravelDestinationRepository extends JpaRepository<TravelDestination, Long> {

	Optional<TravelDestination> findByIdAndBusinessId(Long id, Long businessId);

	Page<TravelDestination> findAllByBusinessId(Long businessId, Pageable pageable);
}
