package com.settings.api.dto.query;

import javax.validation.constraints.NotNull;

public class QueryDefIdRequest {

	@NotNull
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
