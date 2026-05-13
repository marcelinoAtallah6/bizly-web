package com.bm.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bm.api.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	// -- Tenant-scoped finders. Use these from services; never `findById` directly. --
	Optional<Appointment> findByIdAndBusinessId(Long id, Long businessId);

	Page<Appointment> findAllByBusinessId(Long businessId, Pageable pageable);

	@Query("SELECT a FROM Appointment a "
			+ "WHERE a.businessId = :businessId "
			+ "AND a.startTime < :rangeEnd AND a.endTime > :rangeStart "
			+ "ORDER BY a.startTime ASC")
	List<Appointment> findIntersectingRangeForBusiness(@Param("businessId") Long businessId,
			@Param("rangeStart") LocalDateTime rangeStart,
			@Param("rangeEnd") LocalDateTime rangeEnd);
	// ----------------------------------------------------------------------------


	@Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a "
			+ "WHERE a.customerId = :customerId AND a.status <> 'CANCELLED' "
			+ "AND (:excludeId IS NULL OR a.id <> :excludeId) "
			+ "AND a.startTime < :end AND a.endTime > :start")
	boolean existsOverlapForCustomer(@Param("customerId") Long customerId, @Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end, @Param("excludeId") Long excludeAppointmentId);

	@Query("SELECT a FROM Appointment a WHERE a.startTime < :rangeEnd AND a.endTime > :rangeStart ORDER BY a.startTime ASC")
	List<Appointment> findIntersectingRange(@Param("rangeStart") LocalDateTime rangeStart,
			@Param("rangeEnd") LocalDateTime rangeEnd);

	@Query("SELECT a FROM Appointment a WHERE (:customerId IS NULL OR a.customerId = :customerId) "
			+ "AND a.startTime < :rangeEnd AND a.endTime > :rangeStart ORDER BY a.startTime ASC")
	Page<Appointment> findIntersectingRangePagedFiltered(@Param("customerId") Long customerId,
			@Param("rangeStart") LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd,
			Pageable pageable);

	Page<Appointment> findByCustomerIdOrderByStartTimeDesc(Long customerId, Pageable pageable);

	@Query("SELECT COUNT(a) FROM Appointment a "
			+ "WHERE a.startTime >= :dayStart AND a.startTime < :dayEnd")
	long countByStartTimeBetween(@Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);

	@Query("SELECT COUNT(a) FROM Appointment a "
			+ "WHERE a.businessId = :businessId "
			+ "AND a.startTime >= :dayStart AND a.startTime < :dayEnd")
	long countByStartTimeBetweenForBusiness(@Param("businessId") Long businessId,
			@Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);

	@Query("SELECT COUNT(a) FROM Appointment a "
			+ "WHERE a.startTime >= :dayStart AND a.startTime < :dayEnd "
			+ "AND a.status IN ('PENDING','CONFIRMED')")
	long countOpenForRange(@Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);

	@Query("SELECT COUNT(a) FROM Appointment a "
			+ "WHERE a.businessId = :businessId "
			+ "AND a.startTime >= :dayStart AND a.startTime < :dayEnd "
			+ "AND a.status IN ('PENDING','CONFIRMED')")
	long countOpenForRangeForBusiness(@Param("businessId") Long businessId,
			@Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);

	@Query("SELECT COUNT(a) FROM Appointment a "
			+ "WHERE a.startTime >= :now AND a.startTime < :horizon "
			+ "AND a.status IN ('PENDING','CONFIRMED')")
	long countUpcoming(@Param("now") LocalDateTime now, @Param("horizon") LocalDateTime horizon);

	@Query("SELECT COUNT(a) FROM Appointment a "
			+ "WHERE a.businessId = :businessId "
			+ "AND a.startTime >= :now AND a.startTime < :horizon "
			+ "AND a.status IN ('PENDING','CONFIRMED')")
	long countUpcomingForBusiness(@Param("businessId") Long businessId,
			@Param("now") LocalDateTime now, @Param("horizon") LocalDateTime horizon);

	/**
	 * Appointments whose start_time falls in the given window and are still
	 * open. Used by the 60-minute reminder scheduler with a ~20 min window so
	 * a 10 min cron schedule never misses an event.
	 */
	@Query("SELECT a FROM Appointment a "
			+ "WHERE a.startTime >= :from AND a.startTime < :to "
			+ "AND a.status IN ('PENDING','CONFIRMED') "
			+ "ORDER BY a.startTime ASC")
	List<Appointment> findOpenStartingBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	/**
	 * Open appointments whose end_time is in the past — they should have been
	 * marked completed or no-show by now. Used by the past-due scheduler.
	 */
	@Query("SELECT a FROM Appointment a "
			+ "WHERE a.endTime < :now "
			+ "AND a.status IN ('PENDING','CONFIRMED') "
			+ "ORDER BY a.endTime ASC")
	List<Appointment> findPastDueOpen(@Param("now") LocalDateTime now);

	/**
	 * All appointments whose start_time falls inside the day, used by the
	 * morning-brief scheduler. We group these by createdBy in memory to send
	 * one summary per user.
	 */
	@Query("SELECT a FROM Appointment a "
			+ "WHERE a.startTime >= :dayStart AND a.startTime < :dayEnd "
			+ "AND a.status IN ('PENDING','CONFIRMED') "
			+ "ORDER BY a.createdBy ASC, a.startTime ASC")
	List<Appointment> findOpenForDay(@Param("dayStart") LocalDateTime dayStart,
			@Param("dayEnd") LocalDateTime dayEnd);
}
