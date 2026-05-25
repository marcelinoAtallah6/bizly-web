package com.travel.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.api.model.TravelFollowUp;

public interface TravelFollowUpRepository extends JpaRepository<TravelFollowUp, Long> {

	Optional<TravelFollowUp> findByIdAndBusinessId(Long id, Long businessId);

	Page<TravelFollowUp> findAllByBusinessId(Long businessId, Pageable pageable);
}
