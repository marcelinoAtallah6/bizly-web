package com.bm.api.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bm.api.model.CustomerSale;

@Repository
public interface CustomerSaleRepository extends JpaRepository<CustomerSale, Long> {

	Page<CustomerSale> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

	// Tenant-scoped finders.
	Optional<CustomerSale> findByIdAndBusinessId(Long id, Long businessId);

	Page<CustomerSale> findAllByBusinessId(Long businessId, Pageable pageable);

	Page<CustomerSale> findByBusinessIdAndCustomerIdOrderByCreatedAtDesc(Long businessId, Long customerId, Pageable pageable);

	@Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM CustomerSale s "
			+ "WHERE s.businessId = :businessId "
			+ "AND s.createdAt >= :dayStart AND s.createdAt < :dayEnd "
			+ "AND (s.status IS NULL OR s.status <> 'CANCELLED')")
	Double sumTotalForRangeForBusiness(@Param("businessId") Long businessId,
			@Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);

	@Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM CustomerSale s "
			+ "WHERE s.createdAt >= :dayStart AND s.createdAt < :dayEnd "
			+ "AND (s.status IS NULL OR s.status <> 'CANCELLED')")
	Double sumTotalForRange(@Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);

	@Query("SELECT COUNT(s) FROM CustomerSale s "
			+ "WHERE s.createdAt >= :dayStart AND s.createdAt < :dayEnd "
			+ "AND (s.status IS NULL OR s.status <> 'CANCELLED')")
	long countForRange(@Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);

	@Query("SELECT COUNT(s) FROM CustomerSale s "
			+ "WHERE s.businessId = :businessId "
			+ "AND s.createdAt >= :dayStart AND s.createdAt < :dayEnd "
			+ "AND (s.status IS NULL OR s.status <> 'CANCELLED')")
	long countForRangeForBusiness(@Param("businessId") Long businessId,
			@Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);
}
