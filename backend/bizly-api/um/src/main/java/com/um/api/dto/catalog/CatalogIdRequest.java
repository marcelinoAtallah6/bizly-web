package com.um.api.dto.catalog;

import javax.validation.constraints.NotNull;

public class CatalogIdRequest {

	@NotNull
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
