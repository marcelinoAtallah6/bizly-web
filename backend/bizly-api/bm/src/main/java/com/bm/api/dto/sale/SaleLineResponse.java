package com.bm.api.dto.sale;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleLineResponse {

	private String lineType;
	private Long productId;
	private Long serviceId;
	private String productName;
	private Integer quantity;
	private Double unitPrice;
	private Double lineTotal;
}
