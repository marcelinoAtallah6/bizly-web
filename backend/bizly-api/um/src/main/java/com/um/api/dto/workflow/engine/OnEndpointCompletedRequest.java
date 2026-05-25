package com.um.api.dto.workflow.engine;

import java.util.Map;

import javax.validation.constraints.NotNull;

public class OnEndpointCompletedRequest {

	@NotNull
	private Long endpointId;

	private Long businessId;

	private Map<String, Object> context;

	public Long getEndpointId() {
		return endpointId;
	}

	public void setEndpointId(Long endpointId) {
		this.endpointId = endpointId;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	public Map<String, Object> getContext() {
		return context;
	}

	public void setContext(Map<String, Object> context) {
		this.context = context;
	}
}
