package com.um.api.dto.role.permission;

import javax.validation.constraints.NotNull;

public class GetRoleMenuPermissionsRequest {

	@NotNull
	private Long roleId;

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}
}
