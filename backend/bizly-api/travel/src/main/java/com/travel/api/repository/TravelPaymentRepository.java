package com.travel.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.api.model.TravelPayment;

public interface TravelPaymentRepository extends JpaRepository<TravelPayment, Long> {

	Optional<TravelPayment> findByIdAndBusinessId(Long id, Long businessId);

	Page<TravelPayment> findAllByBusinessId(Long businessId, Pageable pageable);
}
