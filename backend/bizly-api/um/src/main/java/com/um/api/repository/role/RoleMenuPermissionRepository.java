package com.um.api.repository.role;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.um.api.model.role.RoleMenuPermission;
import com.um.api.model.role.RoleMenuPermissionId;

@Repository
public interface RoleMenuPermissionRepository extends JpaRepository<RoleMenuPermission, RoleMenuPermissionId> {

	List<RoleMenuPermission> findByIdRoleId(Long roleId);

	long countByIdRoleId(Long roleId);

	void deleteByIdRoleId(Long roleId);

	void deleteByIdMenuId(Long menuId);
}
