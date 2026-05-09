package com.pm.api.dto.get;

import javax.validation.constraints.NotNull;

import com.pm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetProductRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;
}
