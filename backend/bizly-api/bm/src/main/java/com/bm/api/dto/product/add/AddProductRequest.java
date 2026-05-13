package com.bm.api.dto.product.add;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.bm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddProductRequest {

	@NotBlank(message = ApiDefaultValdiation.NAME)
	private String name;

	@NotNull(message = ApiDefaultValdiation.PRICE)
	private Double price;

	private String productImageMimeType;

	private String productImageBase64;

	/** Null or omitted = unlimited stock for checkout. */
	private Integer stockQuantity;
}
