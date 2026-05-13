package com.settings.api.dto.favorite;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Minimal payload for the login flow: just the route to redirect to, or null
 * if the user hasn't pinned a default and should land on the dashboard.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class DefaultRouteResponse {

	private String route;

	public DefaultRouteResponse() {
	}

	public DefaultRouteResponse(String route) {
		this.route = route;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}
}
