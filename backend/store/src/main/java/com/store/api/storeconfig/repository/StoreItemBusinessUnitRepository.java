package com.store.api.storeconfig.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store.api.storeitem.model.StoreItemBusinessUnit;

public interface StoreItemBusinessUnitRepository extends JpaRepository<StoreItemBusinessUnit, Long> {
}
