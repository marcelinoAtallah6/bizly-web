package com.um.api.dto.workflow.engine;

import javax.validation.constraints.NotNull;

public class WorkflowDefinitionIdRequest {

	@NotNull
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
