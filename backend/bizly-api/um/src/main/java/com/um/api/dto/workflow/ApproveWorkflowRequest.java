package com.um.api.dto.workflow;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ApproveWorkflowRequest {

	@NotNull
	private Long instanceId;

	/** Optional note (e.g. reject reason); stored on the instance when rejecting. */
	@Size(max = 4000)
	private String comment;

	public Long getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(Long instanceId) {
		this.instanceId = instanceId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
