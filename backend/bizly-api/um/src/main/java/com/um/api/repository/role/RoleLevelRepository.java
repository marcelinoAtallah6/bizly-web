package com.um.api.repository.role;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.um.api.model.role.RoleLevel;

@Repository
public interface RoleLevelRepository extends JpaRepository<RoleLevel, Long> {

	Optional<RoleLevel> findByCode(String code);

	List<RoleLevel> findAllByOrderBySortOrderAsc();
}
