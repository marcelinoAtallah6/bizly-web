package com.pm.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pm.api.model.CustomerSale;

@Repository
public interface CustomerSaleRepository extends JpaRepository<CustomerSale, Long> {

	Page<CustomerSale> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
}
