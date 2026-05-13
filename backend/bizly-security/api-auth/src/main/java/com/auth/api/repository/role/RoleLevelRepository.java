package com.auth.api.repository.role;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.auth.api.model.role.RoleLevelEntity;

@Repository
public interface RoleLevelRepository extends JpaRepository<RoleLevelEntity, Long> {
	Optional<RoleLevelEntity> findByCode(String code);
}
