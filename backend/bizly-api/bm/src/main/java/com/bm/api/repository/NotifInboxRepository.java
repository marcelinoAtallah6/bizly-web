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

	@Query("SELECT n FROM NotifInbox n WHERE n.username = :username "
			+ "AND (:hideAppointment = false OR n.category <> 'APPOINTMENT') "
			+ "AND (:hideTravelBooking = false OR n.category <> 'TRAVEL_BOOKING') "
			+ "ORDER BY n.createdAt DESC")
	List<NotifInbox> findByUsernameOrderByCreatedAtDesc(@Param("username") String username,
			@Param("hideAppointment") boolean hideAppointment,
			@Param("hideTravelBooking") boolean hideTravelBooking, Pageable pageable);

	@Query("SELECT COUNT(n) FROM NotifInbox n WHERE n.username = :username "
			+ "AND (:hideAppointment = false OR n.category <> 'APPOINTMENT') "
			+ "AND (:hideTravelBooking = false OR n.category <> 'TRAVEL_BOOKING') ")
	long countByUsername(@Param("username") String username, @Param("hideAppointment") boolean hideAppointment,
			@Param("hideTravelBooking") boolean hideTravelBooking);

	@Query("SELECT COUNT(n) FROM NotifInbox n WHERE n.username = :username "
			+ "AND n.readAt IS NULL AND (:hideAppointment = false OR n.category <> 'APPOINTMENT') "
			+ "AND (:hideTravelBooking = false OR n.category <> 'TRAVEL_BOOKING') ")
	long countByUsernameAndReadAtIsNull(@Param("username") String username,
			@Param("hideAppointment") boolean hideAppointment,
			@Param("hideTravelBooking") boolean hideTravelBooking);

	@Query("SELECT n FROM NotifInbox n WHERE n.username = :username "
			+ "AND (n.businessId IS NULL OR n.businessId = :businessId) "
			+ "AND (:hideAppointment = false OR n.category <> 'APPOINTMENT') "
			+ "AND (:hideTravelBooking = false OR n.category <> 'TRAVEL_BOOKING') "
			+ "ORDER BY n.createdAt DESC")
	List<NotifInbox> findInboxForUserAndBusiness(@Param("username") String username,
			@Param("businessId") Long businessId, @Param("hideAppointment") boolean hideAppointment,
			@Param("hideTravelBooking") boolean hideTravelBooking, Pageable pageable);

	@Query("SELECT COUNT(n) FROM NotifInbox n WHERE n.username = :username "
			+ "AND (n.businessId IS NULL OR n.businessId = :businessId) "
			+ "AND (:hideAppointment = false OR n.category <> 'APPOINTMENT') "
			+ "AND (:hideTravelBooking = false OR n.category <> 'TRAVEL_BOOKING') ")
	long countInboxForUserAndBusiness(@Param("username") String username, @Param("businessId") Long businessId,
			@Param("hideAppointment") boolean hideAppointment,
			@Param("hideTravelBooking") boolean hideTravelBooking);

	@Query("SELECT COUNT(n) FROM NotifInbox n WHERE n.username = :username "
			+ "AND n.readAt IS NULL AND (n.businessId IS NULL OR n.businessId = :businessId) "
			+ "AND (:hideAppointment = false OR n.category <> 'APPOINTMENT') "
			+ "AND (:hideTravelBooking = false OR n.category <> 'TRAVEL_BOOKING') ")
	long countUnreadForUserAndBusiness(@Param("username") String username, @Param("businessId") Long businessId,
			@Param("hideAppointment") boolean hideAppointment,
			@Param("hideTravelBooking") boolean hideTravelBooking);

	@Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM NotifInbox n "
			+ "WHERE n.username = :username AND n.resourceType = :rt AND n.resourceId = :rid "
			+ "AND (n.businessId IS NULL OR n.businessId = :businessId)")
	boolean existsForUserAndBusiness(@Param("username") String username,
			@Param("rt") String resourceType, @Param("rid") String resourceId,
			@Param("businessId") Long businessId);

	boolean existsByUsernameAndResourceTypeAndResourceId(String username, String resourceType, String resourceId);

	@Modifying
	@Query("UPDATE NotifInbox n SET n.readAt = :now WHERE n.id = :id AND n.username = :username "
			+ "AND n.readAt IS NULL AND (n.businessId IS NULL OR n.businessId = :businessId)")
	int markReadForBusiness(@Param("id") Long id, @Param("username") String username,
			@Param("businessId") Long businessId, @Param("now") LocalDateTime now);

	@Modifying
	@Query("UPDATE NotifInbox n SET n.readAt = :now WHERE n.username = :username "
			+ "AND n.readAt IS NULL AND (n.businessId IS NULL OR n.businessId = :businessId) "
			+ "AND (:hideAppointment = false OR n.category <> 'APPOINTMENT') "
			+ "AND (:hideTravelBooking = false OR n.category <> 'TRAVEL_BOOKING') ")
	int markAllReadForBusiness(@Param("username") String username, @Param("businessId") Long businessId,
			@Param("hideAppointment") boolean hideAppointment,
			@Param("hideTravelBooking") boolean hideTravelBooking, @Param("now") LocalDateTime now);

	@Modifying
	@Query("UPDATE NotifInbox n SET n.readAt = :now WHERE n.id = :id AND n.username = :username AND n.readAt IS NULL")
	int markRead(@Param("id") Long id, @Param("username") String username, @Param("now") LocalDateTime now);

	@Modifying
	@Query("UPDATE NotifInbox n SET n.readAt = :now WHERE n.username = :username AND n.readAt IS NULL "
			+ "AND (:hideAppointment = false OR n.category <> 'APPOINTMENT') "
			+ "AND (:hideTravelBooking = false OR n.category <> 'TRAVEL_BOOKING') ")
	int markAllRead(@Param("username") String username, @Param("hideAppointment") boolean hideAppointment,
			@Param("hideTravelBooking") boolean hideTravelBooking, @Param("now") LocalDateTime now);
}
