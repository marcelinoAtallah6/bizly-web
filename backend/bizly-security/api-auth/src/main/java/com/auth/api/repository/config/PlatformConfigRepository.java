package com.auth.api.repository.config;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auth.api.model.config.PlatformConfigEntity;

public interface PlatformConfigRepository extends JpaRepository<PlatformConfigEntity, Long> {
}
