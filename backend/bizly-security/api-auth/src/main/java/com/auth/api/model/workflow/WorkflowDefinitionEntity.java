package com.auth.api.model.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** Read-only projection of {@code UM.UM_WORKFLOW_DEFINITION} for registration gating. */
@Entity
@Table(name = "um_workflow_definition", schema = "um")
public class WorkflowDefinitionEntity {

	@Id
	private Long id;

	@Column(name = "action_code", nullable = false, length = 80)
	private String actionCode;

	@Column(name = "version_no", nullable = false)
	private Integer versionNo;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Column(name = "business_id")
	private Long businessId;

	public Long getId() {
		return id;
	}

	public String getActionCode() {
		return actionCode;
	}

	public Integer getVersionNo() {
		return versionNo;
	}

	public String getStatus() {
		return status;
	}

	public Long getBusinessId() {
		return businessId;
	}
}
