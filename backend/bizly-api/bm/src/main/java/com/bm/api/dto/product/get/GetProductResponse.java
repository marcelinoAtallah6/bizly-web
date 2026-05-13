package com.bm.api.dto.product.get;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetProductResponse {

	private Long id;
	private String name;
	private Double price;

	private Integer stockQuantity;

	private String productImageMimeType;
	private String productImageBase64;

}
