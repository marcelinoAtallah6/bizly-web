package com.um.api.dto.menu;

/**
 * One navigable screen linked to {@code UM_MENUS}; {@link #route} is what annotations and JWT use.
 */
public class MenuPermissionRouteRow {

	private Long menuId;
	private String route;
	private String menuPath;
	private Long applicationId;
	private String applicationName;

	public Long getMenuId() {
		return menuId;
	}

	public void setMenuId(Long menuId) {
		this.menuId = menuId;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getMenuPath() {
		return menuPath;
	}

	public void setMenuPath(String menuPath) {
		this.menuPath = menuPath;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
}
