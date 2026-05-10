package com.settings.api.dto.dashboard;

import javax.validation.constraints.NotNull;

public class NavPreferenceRequest {

	@NotNull
	private Long dashboardId;

	@NotNull
	private Boolean hiddenNav;

	public Long getDashboardId() {
		return dashboardId;
	}

	public void setDashboardId(Long dashboardId) {
		this.dashboardId = dashboardId;
	}

	public Boolean getHiddenNav() {
		return hiddenNav;
	}

	public void setHiddenNav(Boolean hiddenNav) {
		this.hiddenNav = hiddenNav;
	}
}
