package com.bm.api.dto.serviceitem.get;

import javax.validation.constraints.NotNull;

import com.bm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetServiceItemRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;
}
