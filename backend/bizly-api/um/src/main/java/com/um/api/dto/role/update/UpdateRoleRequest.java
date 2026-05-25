package com.um.api.dto.role.update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.um.common.ApiDefaultValdiation;

public class UpdateRoleRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;

	@NotBlank(message = ApiDefaultValdiation.ROLE_NAME)
	@Size(max = 100)
	private String name;

	private Integer roleType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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