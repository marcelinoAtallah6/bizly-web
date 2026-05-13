package com.bm.api.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bm.api.model.NotifInbox;

/**
 * Inbox rows are user-scoped AND tenant-scoped. The {@code *AndBusinessId}
 * variants are the only ones that should be called from the inbox service —
 * the un-suffixed legacy methods remain only for system-wide producers
 * (e.g. SUPER_ADMIN announcements) that explicitly set {@code business_id = NULL}.
 */
@Repository
public interface NotifInboxRepository extends JpaRepository<NotifInbox, Long> {

	List<NotifInbox> findByUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

	long countByUsername(String username);

	long countByUsernameAndReadAtIsNull(String username);

	// -- Tenant-scoped finders. Match own-business rows AND global (business_id IS NULL) rows. --
	@Query("SELECT n FROM NotifInbox n WHERE n.username = :username "
			+ "AND (n.businessId IS NULL OR n.businessId = :businessId) "
			+ "ORDER BY n.createdAt DESC")
	List<NotifInbox> findInboxForUserAndBusiness(@Param("username") String username,
			@Param("businessId") Long businessId, Pageable pageable);

	@Query("SELECT COUNT(n) FROM NotifInbox n WHERE n.username = :username "
			+ "AND (n.businessId IS NULL OR n.businessId = :businessId)")
	long countInboxForUserAndBusiness(@Param("username") String username, @Param("businessId") Long businessId);

	@Query("SELECT COUNT(n) FROM NotifInbox n WHERE n.username = :username "
			+ "AND n.readAt IS NULL AND (n.businessId IS NULL OR n.businessId = :businessId)")
	long countUnreadForUserAndBusiness(@Param("username") String username, @Param("businessId") Long businessId);

	@Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NotifInbox n "
			+ "WHERE n.username = :username AND n.resourceType = :rt AND n.resourceId = :rid "
			+ "AND (n.businessId IS NULL OR n.businessId = :businessId)")
	boolean existsForUserAndBusiness(@Param("username") String username,
			@Param("rt") String resourceType, @Param("rid") String resourceId,
			@Param("businessId") Long businessId);

	/**
	 * Used by schedulers for idempotent fan-out: "have we already produced this
	 * exact reminder for this user?". The producer chooses a stable
	 * (resourceType, resourceId) such as ("APPOINTMENT_REMINDER_60", "42@202605120800").
	 */
	boolean existsByUsernameAndResourceTypeAndResourceId(String username, String resourceType, String resourceId);

	@Modifying
	@Query("UPDATE NotifInbox n SET n.readAt = :now WHERE n.id = :id AND n.username = :username "
			+ "AND n.readAt IS NULL AND (n.businessId IS NULL OR n.businessId = :businessId)")
	int markReadForBusiness(@Param("id") Long id, @Param("username") String username,
			@Param("businessId") Long businessId, @Param("now") LocalDateTime now);

	@Modifying
	@Query("UPDATE NotifInbox n SET n.readAt = :now WHERE n.username = :username "
			+ "AND n.readAt IS NULL AND (n.businessId IS NULL OR n.businessId = :businessId)")
	int markAllReadForBusiness(@Param("username") String username, @Param("businessId") Long businessId,
			@Param("now") LocalDateTime now);

	// -- Legacy un-scoped variants kept for system-wide schedulers / housekeeping. --
	@Modifying
	@Query("UPDATE NotifInbox n SET n.readAt = :now WHERE n.id = :id AND n.username = :username AND n.readAt IS NULL")
	int markRead(@Param("id") Long id, @Param("username") String username, @Param("now") LocalDateTime now);

	@Modifying
	@Query("UPDATE NotifInbox n SET n.readAt = :now WHERE n.username = :username AND n.readAt IS NULL")
	int markAllRead(@Param("username") String username, @Param("now") LocalDateTime now);
}
