package com.um.api.repository.user;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.um.api.model.user.User;

/**
 * Tenant-scoped finders. Use these from services; never call inherited
 * {@code findById} / {@code findAll} directly — they bypass {@code business_id}.
 * See {@link com.um.security.BusinessContextHolder}.
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/** Lowest {@code id} wins if duplicate usernames exist (avoids NonUniqueResultException on login-style lookups). */
	Optional<User> findFirstByUsernameOrderByIdAsc(String username);

	Optional<User> findByIdAndBusinessId(Long id, Long businessId);

	Page<User> findAllByBusinessId(Long businessId, Pageable pageable);

	Page<User> findAllByBusinessIdAndIsBusinessOwner(Long businessId, Integer isBusinessOwner, Pageable pageable);

	@Query("SELECT u FROM User u WHERE u.businessId = :businessId "
			+ "AND (u.isBusinessOwner = 1 OR u.userType = :ownerType)")
	Page<User> findBusinessOwnersByBusinessId(@Param("businessId") Long businessId,
			@Param("ownerType") String ownerType, Pageable pageable);

	Page<User> findAllByBusinessIdIsNullAndUserType(String userType, Pageable pageable);

	@Query("SELECT u FROM User u WHERE u.businessId = :businessId AND u.id IN :userIds")
	Page<User> findAllByBusinessIdAndIdIn(@Param("businessId") Long businessId,
			@Param("userIds") Collection<Long> userIds, Pageable pageable);

	@Query("SELECT u.username FROM User u WHERE u.id IN :userIds")
	List<String> findUsernamesByIdIn(@Param("userIds") Collection<Long> userIds);

	@Query("SELECT u FROM User u WHERE u.registrationSource IN ('PUBLIC','SOCIAL') "
			+ "AND u.lastLoginAt IS NULL AND u.expiresAt < :now "
			+ "AND u.status IN ('PENDING_EMAIL_VERIFICATION','PENDING_APPROVAL')")
	List<User> findExpiredDormantPublicUsers(@Param("now") LocalDateTime now);
}
