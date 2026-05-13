package com.bm.api.dto.serviceitem.add;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.bm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddServiceItemRequest {

	@NotBlank(message = ApiDefaultValdiation.NAME)
	private String name;

	private String description;

	@NotNull(message = ApiDefaultValdiation.PRICE)
	private Double price;

	@NotNull(message = ApiDefaultValdiation.DURATION_MINUTES)
	@Min(value = 1, message = "Duration must be at least 1 minute")
	private Integer durationMinutes;

	private Boolean active;
}
