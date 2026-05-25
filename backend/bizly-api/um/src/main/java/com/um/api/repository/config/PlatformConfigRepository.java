package com.um.api.repository.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.um.api.model.config.PlatformConfig;

@Repository
public interface PlatformConfigRepository extends JpaRepository<PlatformConfig, Long> {
}
