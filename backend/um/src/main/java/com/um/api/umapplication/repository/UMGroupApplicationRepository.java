package com.um.api.umapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.um.api.umapplication.model.UMGroupApplication;

@Repository
public interface UMGroupApplicationRepository extends JpaRepository<UMGroupApplication, Long> {
}
