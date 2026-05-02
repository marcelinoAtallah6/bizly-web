package com.store.api.storeitem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.store.api.storeitem.model.StoreItem;

@Repository
public interface StoreItemRepository extends JpaRepository<StoreItem, Long> {
}
