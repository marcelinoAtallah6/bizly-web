package com.travel.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.travel.api.model.TravelBooking;

public interface TravelBookingRepository extends JpaRepository<TravelBooking, Long> {

	Optional<TravelBooking> findByIdAndBusinessId(Long id, Long businessId);

	Page<TravelBooking> findAllByBusinessId(Long businessId, Pageable pageable);

	long countByBusinessId(Long businessId);

	long countByBusinessIdAndStatusIgnoreCase(Long businessId, String status);

	@Query("SELECT b FROM TravelBooking b WHERE b.businessId = :businessId "
			+ "AND b.departureDate IS NOT NULL AND b.departureDate <= :rangeEnd "
			+ "AND (b.returnDate IS NULL OR b.returnDate >= :rangeStart) "
			+ "AND (:packageId IS NULL OR b.packageId = :packageId)")
	List<TravelBooking> findOverlappingInRange(@Param("businessId") Long businessId,
			@Param("packageId") Long packageId, @Param("rangeStart") LocalDate rangeStart,
			@Param("rangeEnd") LocalDate rangeEnd);

	@Query("SELECT b FROM TravelBooking b WHERE b.departureDate = :departureDate "
			+ "AND UPPER(b.status) IN ('DRAFT','ENQUIRY','QUOTED','CONFIRMED') "
			+ "ORDER BY b.businessId ASC, b.departureDate ASC, b.id ASC")
	List<TravelBooking> findOpenDeparturesOnDate(@Param("departureDate") LocalDate departureDate);

	@Query("SELECT b FROM TravelBooking b WHERE b.departureDate IS NOT NULL AND b.departureDate < :today "
			+ "AND UPPER(b.status) IN ('DRAFT','ENQUIRY','QUOTED','CONFIRMED') "
			+ "ORDER BY b.departureDate ASC, b.id ASC")
	List<TravelBooking> findPastDueOpen(@Param("today") LocalDate today);
}
