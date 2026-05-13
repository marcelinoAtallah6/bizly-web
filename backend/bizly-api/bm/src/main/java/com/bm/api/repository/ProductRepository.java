package com.bm.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bm.api.model.Product;

/**
 * All finder methods accept a {@code businessId} so the service layer cannot
 * accidentally read another tenant's data. Use {@link com.bm.security.BusinessContextHolder}
 * to obtain the id.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findByIdAndBusinessId(Long id, Long businessId);

	Page<Product> findAllByBusinessId(Long businessId, Pageable pageable);

	@Query("SELECT p FROM Product p WHERE p.businessId = :businessId AND ("
			+ "  LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')))")
	Page<Product> searchByBusinessId(@Param("businessId") Long businessId, @Param("q") String q, Pageable pageable);

	List<Product> findAllByBusinessIdAndIdIn(Long businessId, List<Long> ids);
}
