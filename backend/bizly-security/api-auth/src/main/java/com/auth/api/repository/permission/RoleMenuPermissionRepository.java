package com.auth.api.repository.permission;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.auth.api.model.permission.RoleMenuPermissionEntity;
import com.auth.api.model.permission.RoleMenuPermissionId;

public interface RoleMenuPermissionRepository extends JpaRepository<RoleMenuPermissionEntity, RoleMenuPermissionId> {

	List<RoleMenuPermissionEntity> findByIdRoleId(Long roleId);

	long countByIdRoleId(Long roleId);
}
