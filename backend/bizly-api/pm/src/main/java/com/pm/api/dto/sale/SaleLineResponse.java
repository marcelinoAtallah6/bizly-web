package com.pm.api.dto.sale;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleLineResponse {

	private Long productId;
	private String productName;
	private Integer quantity;
	private Double unitPrice;
	private Double lineTotal;
}
