package com.bm.api.dto.product.delete;

import javax.validation.constraints.NotNull;

import com.bm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteProductRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;
}
