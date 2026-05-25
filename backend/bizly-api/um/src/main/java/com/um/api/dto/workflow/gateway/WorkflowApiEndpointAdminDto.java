package com.um.api.dto.workflow.gateway;

public class WorkflowApiEndpointAdminDto {

	private Long id;
	private String serviceKey;
	private String httpMethod;
	private String pathAntPattern;
	private Long menuId;
	private String screenRoute;
	private String actionCode;
	private Integer priority;
	private boolean active;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
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

	public Long getMenuId() {
		return menuId;
	}

	public void setMenuId(Long menuId) {
		this.menuId = menuId;
	}

	public String getScreenRoute() {
		return screenRoute;
	}

	public void setScreenRoute(String screenRoute) {
		this.screenRoute = screenRoute;
	}

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
