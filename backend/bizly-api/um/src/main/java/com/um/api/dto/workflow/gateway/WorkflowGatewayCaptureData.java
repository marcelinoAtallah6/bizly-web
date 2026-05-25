package com.um.api.dto.workflow.gateway;

public class WorkflowGatewayCaptureData {

	private Long instanceId;

	public WorkflowGatewayCaptureData() {
	}

	public WorkflowGatewayCaptureData(Long instanceId) {
		this.instanceId = instanceId;
	}

	public Long getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(Long instanceId) {
		this.instanceId = instanceId;
	}
}
