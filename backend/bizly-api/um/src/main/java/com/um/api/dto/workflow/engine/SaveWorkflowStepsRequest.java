package com.um.api.dto.workflow.engine;

import java.util.List;

import javax.validation.constraints.NotNull;

public class SaveWorkflowStepsRequest {

	@NotNull
	private Long definitionId;

	@NotNull
	private List<WorkflowStepDto> steps;

	public Long getDefinitionId() {
		return definitionId;
	}

	public void setDefinitionId(Long definitionId) {
		this.definitionId = definitionId;
	}

	public List<WorkflowStepDto> getSteps() {
		return steps;
	}

	public void setSteps(List<WorkflowStepDto> steps) {
		this.steps = steps;
	}
}
