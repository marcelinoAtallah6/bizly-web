package com.um.api.dto.workflow.gateway;

import javax.validation.constraints.NotBlank;

public class WorkflowGatewayMatchRequest {

	@NotBlank
	private String path;

	@NotBlank
	private String method;

	private Long businessId;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
}
