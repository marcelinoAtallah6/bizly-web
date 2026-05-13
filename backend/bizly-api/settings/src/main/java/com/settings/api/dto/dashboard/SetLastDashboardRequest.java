package com.settings.api.dto.dashboard;

/** {@code id == null} clears the user's saved dashboard. */
public class SetLastDashboardRequest {

	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
