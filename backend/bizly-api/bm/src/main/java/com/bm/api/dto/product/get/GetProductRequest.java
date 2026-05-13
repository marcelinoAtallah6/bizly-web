package com.bm.api.dto.product.get;

import javax.validation.constraints.NotNull;

import com.bm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetProductRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;
}
