package com.um.api.dto.workflow;

import javax.validation.constraints.NotNull;

public class DeleteWorkflowConfigRequest {

	@NotNull
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
