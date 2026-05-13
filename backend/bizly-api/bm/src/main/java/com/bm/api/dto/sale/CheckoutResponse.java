package com.bm.api.dto.sale;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutResponse {

	private Long saleId;
	private Double totalAmount;
}
