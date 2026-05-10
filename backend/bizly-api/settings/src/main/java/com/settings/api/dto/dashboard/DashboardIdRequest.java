package com.settings.api.dto.dashboard;

import javax.validation.constraints.NotNull;

public class DashboardIdRequest {

	@NotNull
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
