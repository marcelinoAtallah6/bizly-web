package com.kyc.api.dto.get;

import javax.validation.constraints.NotNull;

import com.kyc.common.ApiDefaultValdiation;

public class GetCustomerRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
