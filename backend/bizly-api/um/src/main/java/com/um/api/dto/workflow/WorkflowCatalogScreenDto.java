package com.um.api.dto.workflow;

import java.util.List;

/**
 * One navigable screen (from {@code UM_MENUS}) and the permission verbs granted for that menu on
 * {@code UM_ROLE_MENU_PERM} (union across roles).
 */
public class WorkflowCatalogScreenDto {

	private String route;
	private String menuLabel;
	private List<String> actions;

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getMenuLabel() {
		return menuLabel;
	}

	public void setMenuLabel(String menuLabel) {
		this.menuLabel = menuLabel;
	}

	public List<String> getActions() {
		return actions;
	}

	public void setActions(List<String> actions) {
		this.actions = actions;
	}
}
