package com.auth.api.repository.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.auth.api.model.user.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	/** Resolves by username; if duplicates exist, returns the lowest {@code id} row (avoids NonUniqueResultException). */
	Optional<UserEntity> findFirstByUsernameOrderByIdAsc(String username);

	boolean existsByUsername(String username);

	Optional<UserEntity> findByEmailIgnoreCase(String email);

	/** Admin context-switcher autocomplete. Matches username / email / first / last name. */
	@Query("SELECT u FROM UserEntity u "
			+ "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%')) "
			+ "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) "
			+ "   OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :q, '%')) "
			+ "   OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :q, '%')) "
			+ "ORDER BY u.username ASC")
	List<UserEntity> searchByUsernameOrEmail(@Param("q") String q, Pageable pageable);

	List<UserEntity> findAllByBusinessId(Long businessId, Pageable pageable);
}
