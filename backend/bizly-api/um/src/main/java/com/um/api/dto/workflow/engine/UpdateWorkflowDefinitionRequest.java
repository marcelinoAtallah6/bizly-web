package com.um.api.dto.workflow.engine;

import javax.validation.constraints.NotNull;

public class UpdateWorkflowDefinitionRequest {

	@NotNull
	private Long id;

	private String displayName;
	private String notes;
	private String workflowType;
	private Boolean roleRestricted;
	private Boolean allowChildRoleInherit;
	private String roleCodesJson;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getWorkflowType() {
		return workflowType;
	}

	public void setWorkflowType(String workflowType) {
		this.workflowType = workflowType;
	}

	public Boolean getRoleRestricted() {
		return roleRestricted;
	}

	public void setRoleRestricted(Boolean roleRestricted) {
		this.roleRestricted = roleRestricted;
	}

	public Boolean getAllowChildRoleInherit() {
		return allowChildRoleInherit;
	}

	public void setAllowChildRoleInherit(Boolean allowChildRoleInherit) {
		this.allowChildRoleInherit = allowChildRoleInherit;
	}

	public String getRoleCodesJson() {
		return roleCodesJson;
	}

	public void setRoleCodesJson(String roleCodesJson) {
		this.roleCodesJson = roleCodesJson;
	}
}
