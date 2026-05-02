package com.um.api.umapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.um.api.umapplication.model.UMApplication;

@Repository
public interface UMApplicationRepository extends JpaRepository<UMApplication, Long> {
}
