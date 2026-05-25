package com.um.api.dto.workflow.engine;

import java.util.ArrayList;
import java.util.List;

public class WorkflowDefinitionDetailDto extends WorkflowDefinitionRowDto {

	private List<WorkflowStepDto> steps = new ArrayList<>();

	public List<WorkflowStepDto> getSteps() {
		return steps;
	}

	public void setSteps(List<WorkflowStepDto> steps) {
		this.steps = steps != null ? steps : new ArrayList<>();
	}
}
