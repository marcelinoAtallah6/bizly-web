package com.notification.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.notification.domain.NotificationDispatchLogEntity;

public interface NotificationDispatchLogRepository extends JpaRepository<NotificationDispatchLogEntity, Long> {

	boolean existsByUserIdAndProcessTypeAndCreatedAtGreaterThanEqual(Long userId, String processType,
			LocalDateTime since);
}
