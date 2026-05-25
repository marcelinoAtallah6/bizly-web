package com.auth.api.model.workflow;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Auth-service read projection of {@code UM.UM_WORKFLOW_CONFIG} (global built-in rows only).
 */
@Entity
@Table(name = "um_workflow_config", schema = "um")
public class WorkflowConfigEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "screen_name", nullable = false, length = 200)
	private String screenName;

	@Column(name = "action_name", nullable = false, length = 200)
	private String actionName;

	@Column(name = "has_workflow", nullable = false)
	private Integer hasWorkflow;

	@Column(name = "built_in_key", length = 80)
	private String builtInKey;

	@Column(name = "business_id")
	private Long businessId;

	public Long getId() { return id; }
	public String getScreenName() { return screenName; }
	public String getActionName() { return actionName; }
	public boolean isHasWorkflow() { return hasWorkflow != null && hasWorkflow == 1; }
	public String getBuiltInKey() { return builtInKey; }
	public Long getBusinessId() { return businessId; }
}
