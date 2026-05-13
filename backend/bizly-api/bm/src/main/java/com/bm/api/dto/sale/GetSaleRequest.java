package com.bm.api.dto.sale;

import javax.validation.constraints.NotNull;

import com.bm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetSaleRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;
}
