package com.travel.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.api.model.TravelClient;

public interface TravelClientRepository extends JpaRepository<TravelClient, Long> {

	Optional<TravelClient> findByIdAndBusinessId(Long id, Long businessId);

	Page<TravelClient> findAllByBusinessId(Long businessId, Pageable pageable);

	long countByBusinessId(Long businessId);
}
