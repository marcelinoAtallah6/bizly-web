package com.um.api.dto.workflow.gateway;

import javax.validation.constraints.NotNull;

public class UpdateWorkflowEndpointRequest {

	@NotNull
	private Long id;
	private String pathAntPattern;
	private String httpMethod;
	private Integer priority;
	private Boolean active;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPathAntPattern() {
		return pathAntPattern;
	}

	public void setPathAntPattern(String pathAntPattern) {
		this.pathAntPattern = pathAntPattern;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
}
