package com.um.api.dto.workflow.engine;

/**
 * A screen route + permission verb the current principal's role(s) may perform.
 */
public class ApplicationActionTargetDto {

	private String screenRoute;
	private String menuLabel;
	private String actionCode;

	public String getScreenRoute() {
		return screenRoute;
	}

	public void setScreenRoute(String screenRoute) {
		this.screenRoute = screenRoute;
	}

	public String getMenuLabel() {
		return menuLabel;
	}

	public void setMenuLabel(String menuLabel) {
		this.menuLabel = menuLabel;
	}

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}
}
