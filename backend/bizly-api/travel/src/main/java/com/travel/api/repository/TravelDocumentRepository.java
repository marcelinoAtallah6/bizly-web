package com.travel.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.api.model.TravelDocument;

public interface TravelDocumentRepository extends JpaRepository<TravelDocument, Long> {

	Optional<TravelDocument> findByIdAndBusinessId(Long id, Long businessId);

	Page<TravelDocument> findAllByBusinessId(Long businessId, Pageable pageable);
}
