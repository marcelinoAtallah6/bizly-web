package com.um.api.dto.menu;

import java.util.List;

/**
 * All screens that participate in the matrix plus the action codes (VIEW, ADD, EDIT, DELETE).
 */
public class PermissionMetadataResponse {

	private List<String> actions;
	private List<MenuPermissionRouteRow> menus;

	public List<String> getActions() {
		return actions;
	}

	public void setActions(List<String> actions) {
		this.actions = actions;
	}

	public List<MenuPermissionRouteRow> getMenus() {
		return menus;
	}

	public void setMenus(List<MenuPermissionRouteRow> menus) {
		this.menus = menus;
	}
}
