package com.settings.api.dto.favorite;

/**
 * {@code route = null} explicitly clears any existing default (= go back to the
 * dashboard on next login). A blank/missing field is treated the same way.
 */
public class SetDefaultFavoriteRequest {

	private String route;

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}
}
