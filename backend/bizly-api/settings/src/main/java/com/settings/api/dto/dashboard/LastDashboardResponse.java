package com.settings.api.dto.dashboard;

public class LastDashboardResponse {

	/** Null when no preference is stored (or saved dashboard is no longer reachable). */
	private Long dashboardId;

	public Long getDashboardId() {
		return dashboardId;
	}

	public void setDashboardId(Long dashboardId) {
		this.dashboardId = dashboardId;
	}
}
