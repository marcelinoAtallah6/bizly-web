package com.settings.api.dto.favorite;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class FavoriteRouteRequest {

	@NotBlank
	@Size(max = 400)
	private String route;

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}
}
