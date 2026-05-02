package com.kyc.api.customers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kyc.api.customers.model.KYCCustomers;

@Repository
public interface KYCCustomersRepository extends JpaRepository<KYCCustomers, Long> {
}
