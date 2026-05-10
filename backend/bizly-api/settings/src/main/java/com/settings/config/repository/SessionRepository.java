package com.settings.config.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.settings.config.model.session.SessionEntity;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

	public Optional<SessionEntity> findBySessionId(String sessionId);

}