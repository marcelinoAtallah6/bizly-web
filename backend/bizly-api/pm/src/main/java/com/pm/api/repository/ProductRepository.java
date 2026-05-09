package com.pm.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pm.api.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}