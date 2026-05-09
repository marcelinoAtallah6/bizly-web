package com.pm.api.dto.sale;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.pm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutLineRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long productId;

	@NotNull
	@Min(value = 1, message = "Quantity must be at least 1")
	private Integer quantity;
}
