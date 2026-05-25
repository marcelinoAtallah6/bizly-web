package com.travel.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.api.model.TravelTripRequest;

public interface TravelTripRequestRepository extends JpaRepository<TravelTripRequest, Long> {

	Optional<TravelTripRequest> findByIdAndBusinessId(Long id, Long businessId);

	Page<TravelTripRequest> findAllByBusinessId(Long businessId, Pageable pageable);
}
