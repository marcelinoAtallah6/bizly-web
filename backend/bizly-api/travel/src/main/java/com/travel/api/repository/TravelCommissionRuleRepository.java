package com.travel.api.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.travel.api.model.TravelCommissionRule;

public interface TravelCommissionRuleRepository extends JpaRepository<TravelCommissionRule, Long> {

	Optional<TravelCommissionRule> findByIdAndBusinessId(Long id, Long businessId);

	Page<TravelCommissionRule> findAllByBusinessId(Long businessId, Pageable pageable);
}
