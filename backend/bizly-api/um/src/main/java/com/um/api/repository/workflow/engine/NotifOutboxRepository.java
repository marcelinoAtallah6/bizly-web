package com.um.api.repository.workflow.engine;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.um.api.model.workflow.engine.NotifOutbox;

public interface NotifOutboxRepository extends JpaRepository<NotifOutbox, Long> {

	Optional<NotifOutbox> findByIdempotencyKey(String idempotencyKey);
}
