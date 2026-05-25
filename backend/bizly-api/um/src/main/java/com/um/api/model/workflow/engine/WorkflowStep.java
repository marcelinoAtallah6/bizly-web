package com.um.api.model.workflow.engine;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.um.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.WORKFLOW_STEP_TABLE, schema = DatabaseConstants.SCHEMA)
public class WorkflowStep {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wf_step_seq")
	@SequenceGenerator(name = "wf_step_seq", sequenceName = DatabaseConstants.WORKFLOW_STEP_SEQ, allocationSize = 1)
	private Long id;

	@Column(name = "definition_id", nullable = false)
	private Long definitionId;

	@Column(name = "step_order", nullable = false)
	private Integer stepOrder;

	@Column(name = "step_type", nullable = false, length = 30)
	private String stepType;

	@Column(name = "is_active", nullable = false)
	private Integer isActive;

	@Lob
	@Column(name = "config_json", nullable = false)
	private String configJson;

	@Column(name = "workflow_config_id")
	private Long workflowConfigId;

	@Column(name = "task_key", length = 80)
	private String taskKey;

	@Column(name = "display_label", length = 200)
	private String displayLabel;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDefinitionId() {
		return definitionId;
	}

	public void setDefinitionId(Long definitionId) {
		this.definitionId = definitionId;
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
		return isActive != null && isActive == 1;
	}

	public void setActive(boolean active) {
		this.isActive = active ? 1 : 0;
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
