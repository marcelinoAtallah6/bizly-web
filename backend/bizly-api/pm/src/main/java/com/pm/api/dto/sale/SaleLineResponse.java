package com.pm.api.dto.sale;

import lombok.Getter;
import lombok.Setter;

/**
 * Wire shape of a single {@link com.pm.api.model.CustomerSaleLine} row. Mirrors BM so a sale created
 * by {@code /pm/sale/checkout} renders identically to historical BM-created sales in the customer
 * details / payments history screens.
 */
@Getter
@Setter
public class SaleLineResponse {

	/** {@code PRODUCT} or {@code SERVICE}. */
	private String lineType;
	private Long productId;
	private Long serviceId;
	private String productName;
	private Integer quantity;
	private Double unitPrice;
	private Double lineTotal;
}
