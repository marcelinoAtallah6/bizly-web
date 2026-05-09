package com.um.api.dto.role.get;

import javax.validation.constraints.NotNull;

import com.um.common.ApiDefaultValdiation;

public class GetRoleRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}