package com.travel.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.api.model.TravelInvoice;

public interface TravelInvoiceRepository extends JpaRepository<TravelInvoice, Long> {

	Optional<TravelInvoice> findByIdAndBusinessId(Long id, Long businessId);

	Page<TravelInvoice> findAllByBusinessId(Long businessId, Pageable pageable);
}
