package com.um.api.dto.catalog;

public class CatalogSaveResponse {

	private Long id;

	public CatalogSaveResponse() {
	}

	public CatalogSaveResponse(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
