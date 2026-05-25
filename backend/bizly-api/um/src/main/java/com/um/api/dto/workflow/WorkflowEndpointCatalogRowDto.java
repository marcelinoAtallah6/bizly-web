package com.um.api.dto.workflow;

/**
 * One {@code UM_WORKFLOW_API_ENDPOINT} row formatted for the workflow configuration UI (screen + action
 * choices tied to gateway Ant patterns).
 */
public class WorkflowEndpointCatalogRowDto {

	private Long endpointId;
	private String screenRoute;
	private String menuLabel;
	private String actionCode;
	private String httpMethod;
	private String pathAntPattern;
	/** Human-readable line for dropdowns, e.g. {@code Products — ADD (/pm/product/add)}. */
	private String displayLabel;

	public Long getEndpointId() {
		return endpointId;
	}

	public void setEndpointId(Long endpointId) {
		this.endpointId = endpointId;
	}

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

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getPathAntPattern() {
		return pathAntPattern;
	}

	public void setPathAntPattern(String pathAntPattern) {
		this.pathAntPattern = pathAntPattern;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}
}
