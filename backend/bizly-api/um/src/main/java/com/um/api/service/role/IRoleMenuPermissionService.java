package com.um.api.service.role;

import com.um.api.dto.role.permission.GetRoleMenuPermissionsRequest;
import com.um.api.dto.role.permission.RoleMenuPermissionRowResponse;
import com.um.api.dto.role.permission.SaveRoleMenuPermissionsRequest;

import java.util.List;

public interface IRoleMenuPermissionService {

	List<RoleMenuPermissionRowResponse> getPermissions(GetRoleMenuPermissionsRequest request);

	void savePermissions(SaveRoleMenuPermissionsRequest request);
}
