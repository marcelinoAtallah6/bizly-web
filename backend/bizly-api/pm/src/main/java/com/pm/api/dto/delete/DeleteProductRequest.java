package com.pm.api.dto.delete;

import javax.validation.constraints.NotNull;

import com.pm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteProductRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;
}
