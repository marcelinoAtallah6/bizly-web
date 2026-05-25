package com.um.api.repository.business;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.um.api.model.business.Business;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

	boolean existsByBusinessNameIgnoreCase(String businessName);

	Optional<Business> findByBusinessNameIgnoreCase(String businessName);
}
