package com.pm.api.dto.sale;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * One line of a unified {@code /pm/sale/checkout} cart. The line is a product purchase when
 * {@link #productId} is set, or a service purchase when {@link #serviceId} is set. Exactly one of the
 * two must be populated — the service layer rejects lines that supply both or neither.
 */
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
