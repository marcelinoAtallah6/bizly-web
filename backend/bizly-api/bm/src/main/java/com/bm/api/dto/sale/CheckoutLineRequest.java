package com.bm.api.dto.sale;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.bm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutLineRequest {

	/** Set for product (inventory) lines; omit when paying for a service. */
	private Long productId;

	/** Set for BM service lines; omit when selling a product. */
	private Long serviceId;

	@NotNull
	@Min(value = 1, message = "Quantity must be at least 1")
	private Integer quantity;
}
