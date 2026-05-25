package com.um.api.dto.workflow.engine;

import java.util.Map;

import javax.validation.constraints.NotBlank;

public class TriggerWorkflowActionRequest {

	@NotBlank
	private String actionCode;

	private Long businessId;

	private Map<String, Object> context;

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
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
