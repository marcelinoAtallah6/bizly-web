package com.auth.api.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auth.api.model.user.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

	/**
	 * Lowest {@code id} wins when duplicate role names exist (avoids NonUniqueResultException during login JWT
	 * resolution).
	 */
	Optional<RoleEntity> findFirstByNameIgnoreCaseOrderByIdAsc(String name);
}
