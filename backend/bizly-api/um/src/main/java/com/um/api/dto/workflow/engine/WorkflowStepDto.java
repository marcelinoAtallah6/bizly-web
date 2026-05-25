package com.um.api.dto.workflow.engine;

public class WorkflowStepDto {

	private Long id;
	private Integer stepOrder;
	private String stepType;
	private boolean active;
	private String configJson;
	private Long workflowConfigId;
	private String taskKey;
	private String displayLabel;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getStepOrder() {
		return stepOrder;
	}

	public void setStepOrder(Integer stepOrder) {
		this.stepOrder = stepOrder;
	}

	public String getStepType() {
		return stepType;
	}

	public void setStepType(String stepType) {
		this.stepType = stepType;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getConfigJson() {
		return configJson;
	}

	public void setConfigJson(String configJson) {
		this.configJson = configJson;
	}

	public Long getWorkflowConfigId() {
		return workflowConfigId;
	}

	public void setWorkflowConfigId(Long workflowConfigId) {
		this.workflowConfigId = workflowConfigId;
	}

	public String getTaskKey() {
		return taskKey;
	}

	public void setTaskKey(String taskKey) {
		this.taskKey = taskKey;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}
}
