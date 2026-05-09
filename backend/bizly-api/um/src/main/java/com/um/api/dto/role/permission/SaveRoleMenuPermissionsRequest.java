package com.um.api.dto.role.permission;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class SaveRoleMenuPermissionsRequest {

	@NotNull
	private Long roleId;

	@Valid
	private List<RoleMenuPermissionEntryDto> permissions = new ArrayList<>();

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public List<RoleMenuPermissionEntryDto> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<RoleMenuPermissionEntryDto> permissions) {
		this.permissions = permissions;
	}
}
