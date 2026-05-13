package com.settings.api.dto.favorite;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserFavoritesResponse {

	private List<UserFavoriteItem> items;
	private String defaultRoute;

	public List<UserFavoriteItem> getItems() {
		return items;
	}

	public void setItems(List<UserFavoriteItem> items) {
		this.items = items;
	}

	public String getDefaultRoute() {
		return defaultRoute;
	}

	public void setDefaultRoute(String defaultRoute) {
		this.defaultRoute = defaultRoute;
	}
}
