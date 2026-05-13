package com.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notification.domain.BroadcastRecipientEntity;

public interface BroadcastRecipientRepository extends JpaRepository<BroadcastRecipientEntity, Long> {

	boolean existsByBroadcastMessageIdAndRecipientEmailIgnoreCase(long broadcastMessageId, String recipientEmail);
}
