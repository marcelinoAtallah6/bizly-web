package com.um.api.dto.workflow.engine;

public class WorkflowActionDto {

	private String actionCode;
	private String displayName;
	private String description;
	private String moduleKey;
	private String triggerKind;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getModuleKey() {
		return moduleKey;
	}

	public void setModuleKey(String moduleKey) {
		this.moduleKey = moduleKey;
	}

	public String getTriggerKind() {
		return triggerKind;
	}

	public void setTriggerKind(String triggerKind) {
		this.triggerKind = triggerKind;
	}
}
