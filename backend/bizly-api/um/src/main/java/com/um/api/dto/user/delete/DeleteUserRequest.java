package com.um.api.dto.user.delete;

import javax.validation.constraints.NotNull;

import com.um.common.ApiDefaultValdiation;

public class DeleteUserRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
