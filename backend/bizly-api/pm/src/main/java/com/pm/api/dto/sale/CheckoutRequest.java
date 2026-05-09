package com.pm.api.dto.sale;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.pm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long customerId;

	@Valid
	@NotEmpty(message = "At least one line item is required")
	private List<CheckoutLineRequest> lines;

	/** Optional snapshot label for history (e.g. customer full name). */
	private String customerDisplayName;
}
