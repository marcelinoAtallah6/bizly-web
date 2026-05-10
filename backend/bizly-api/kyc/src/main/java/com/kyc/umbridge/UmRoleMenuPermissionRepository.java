package com.kyc.umbridge;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UmRoleMenuPermissionRepository
		extends JpaRepository<UmRoleMenuPermissionEntity, UmRoleMenuPermissionId> {

	long countByIdRoleId(Long roleId);
}
