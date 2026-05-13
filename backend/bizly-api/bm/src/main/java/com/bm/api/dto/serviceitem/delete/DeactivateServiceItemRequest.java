package com.bm.api.dto.serviceitem.delete;

import javax.validation.constraints.NotNull;

import com.bm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeactivateServiceItemRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;
}
