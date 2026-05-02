package com.store.api.storeconfig.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store.api.storeconfig.model.StoreBusinessUnit;

public interface StoreBusinessUnitRepository extends JpaRepository<StoreBusinessUnit, Long> {
}
