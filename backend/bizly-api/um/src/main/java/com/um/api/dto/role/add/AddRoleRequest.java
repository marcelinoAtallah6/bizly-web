package com.um.api.dto.role.add;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.um.common.ApiDefaultValdiation;

public class AddRoleRequest {

	@NotBlank(message = ApiDefaultValdiation.ROLE_NAME)
	@Size(max = 100)
	private String name;

	@NotNull(message = ApiDefaultValdiation.ROLE_TYPE)
	private Integer roleType;

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
}