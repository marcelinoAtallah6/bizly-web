package com.um.api.umapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.um.api.umapplication.model.UMMenu;

@Repository
public interface UMMenuRepository extends JpaRepository<UMMenu, Long> {
}
