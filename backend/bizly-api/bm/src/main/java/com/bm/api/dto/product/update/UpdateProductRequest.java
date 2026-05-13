package com.bm.api.dto.product.update;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.bm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;

	@NotBlank(message = ApiDefaultValdiation.NAME)
	private String name;

	@NotNull(message = ApiDefaultValdiation.PRICE)
	private Double price;

	private Boolean clearProductImage;

	private String productImageMimeType;

	private String productImageBase64;

	private Integer stockQuantity;
}