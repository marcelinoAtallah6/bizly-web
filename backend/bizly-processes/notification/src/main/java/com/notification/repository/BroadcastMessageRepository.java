package com.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.notification.domain.BroadcastMessageEntity;

public interface BroadcastMessageRepository extends JpaRepository<BroadcastMessageEntity, Long> {

	@Query("select b.id from BroadcastMessageEntity b where b.status = 'PENDING' and b.deliveryRequested = true order by b.createdAt asc")
	List<Long> findIdsPendingDelivery(org.springframework.data.domain.Pageable pageable);

	@Modifying
	@Transactional
	@Query("update BroadcastMessageEntity b set b.status = 'PROCESSING' where b.id = :id and b.status = 'PENDING' and b.deliveryRequested = true")
	int tryClaim(@Param("id") long id);

	@Modifying
	@Transactional
	@Query("update BroadcastMessageEntity b set b.status = :newStatus, b.sentAt = :sentAt where b.id = :id")
	void updateTerminalStatus(@Param("id") long id, @Param("newStatus") String newStatus,
			@Param("sentAt") java.time.LocalDateTime sentAt);
}
