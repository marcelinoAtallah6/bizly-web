package com.um.api.dto.role.add;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.um.common.ApiDefaultValdiation;

public class AddRoleRequest {

	@NotBlank(message = ApiDefaultValdiation.ROLE_NAME)
	@Size(max = 100)
	private String name;

	/** Optional classification code; defaults to 1 for admin roles, 200+ for business templates. */
	private Integer roleType;

	@NotNull(message = "Role level is required")
	private Long roleLevelId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getRoleType() {
		return roleType;
	}

	public void setRoleType(Integer roleType) {
		this.roleType = roleType;
	}

	public Long getRoleLevelId() {
		return roleLevelId;
	}

	public void setRoleLevelId(Long roleLevelId) {
		this.roleLevelId = roleLevelId;
	}
}