package com.um.api.dto.workflow.engine;

import javax.validation.constraints.NotBlank;

public class CreateWorkflowDefinitionRequest {

	@NotBlank
	private String actionCode;

	private String displayName;
	private String notes;
	/** null = global (super-admin); set = tenant-specific */
	private Long businessId;
	private String workflowType;

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	public String getWorkflowType() {
		return workflowType;
	}

	public void setWorkflowType(String workflowType) {
		this.workflowType = workflowType;
	}
}
