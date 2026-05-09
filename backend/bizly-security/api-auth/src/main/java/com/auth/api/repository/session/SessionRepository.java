package com.auth.api.repository.session;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auth.api.model.session.SessionEntity;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

	public Optional<SessionEntity> findBySessionId(String sessionId);
	
	public Optional<SessionEntity> findByUserIdAndDeviceId(Long userId, String deviceId);

}