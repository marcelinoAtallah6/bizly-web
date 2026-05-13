package com.settings.api.dto.favorite;

import java.util.List;

/**
 * Submit the full ordered list of routes for the current user. Backend sets
 * each row's pin_order to its index in the array. Missing routes are ignored;
 * unknown routes are silently dropped.
 */
public class ReorderFavoritesRequest {

	private List<String> routes;

	public List<String> getRoutes() {
		return routes;
	}

	public void setRoutes(List<String> routes) {
		this.routes = routes;
	}
}
