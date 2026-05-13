package com.bm.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bm.api.model.customer.KycCustomerRef;

public interface KycCustomerRefRepository extends JpaRepository<KycCustomerRef, Long> {
}
