package com.travel.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.api.model.TravelVisaApplication;

public interface TravelVisaApplicationRepository extends JpaRepository<TravelVisaApplication, Long> {

	Optional<TravelVisaApplication> findByIdAndBusinessId(Long id, Long businessId);

	Page<TravelVisaApplication> findAllByBusinessId(Long businessId, Pageable pageable);
}
