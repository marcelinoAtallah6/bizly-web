package com.kyc.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kyc.api.model.customer.KycCustomer;

public interface KycCustomerRepository extends JpaRepository<KycCustomer, Long> {
}
