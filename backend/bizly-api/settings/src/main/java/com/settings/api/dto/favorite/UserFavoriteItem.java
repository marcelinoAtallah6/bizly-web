package com.settings.api.dto.favorite;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserFavoriteItem {

	private String menuRoute;
	private int pinOrder;
	/*
	 * Serialized explicitly as "isDefault" so the frontend can use a non-reserved
	 * property name on the TypeScript side. Without the annotation Jackson would
	 * strip the "is" prefix and emit "default".
	 */
	@JsonProperty("isDefault")
	private boolean isDefault;

	public UserFavoriteItem() {
	}

	public UserFavoriteItem(String menuRoute, int pinOrder, boolean isDefault) {
		this.menuRoute = menuRoute;
		this.pinOrder = pinOrder;
		this.isDefault = isDefault;
	}

	public String getMenuRoute() {
		return menuRoute;
	}

	public void setMenuRoute(String menuRoute) {
		this.menuRoute = menuRoute;
	}

	public int getPinOrder() {
		return pinOrder;
	}

	public void setPinOrder(int pinOrder) {
		this.pinOrder = pinOrder;
	}

	@JsonProperty("isDefault")
	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
}
