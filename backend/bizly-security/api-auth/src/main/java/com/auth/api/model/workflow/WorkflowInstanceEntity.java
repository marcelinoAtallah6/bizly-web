package com.auth.api.model.workflow;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Auth-service projection of {@code UM.UM_WORKFLOW_INSTANCE} for onboarding inserts.
 */
@Entity
@Table(name = "um_workflow_instance", schema = "um")
public class WorkflowInstanceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wf_inst_seq")
	@SequenceGenerator(name = "wf_inst_seq", sequenceName = "S_UM_WORKFLOW_INSTANCE", schema = "um", allocationSize = 1)
	private Long id;

	@Column(name = "workflow_config_id", nullable = false)
	private Long workflowConfigId;

	@Column(name = "business_id", nullable = false)
	private Long businessId;

	@Column(name = "triggered_by_user_id")
	private Long triggeredByUserId;

	@Column(name = "triggered_by_username", length = 120)
	private String triggeredByUsername;

	@Column(name = "current_level", nullable = false)
	private Integer currentLevel;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Lob
	@Column(name = "payload_json")
	private String payloadJson;

	@Column(name = "screen_name", length = 200)
	private String screenName;

	@Column(name = "action_name", length = 200)
	private String actionName;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public Long getId() { return id; }
	public void setWorkflowConfigId(Long workflowConfigId) { this.workflowConfigId = workflowConfigId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
	public void setTriggeredByUserId(Long triggeredByUserId) { this.triggeredByUserId = triggeredByUserId; }
	public void setTriggeredByUsername(String triggeredByUsername) { this.triggeredByUsername = triggeredByUsername; }
	public void setCurrentLevel(Integer currentLevel) { this.currentLevel = currentLevel; }
	public void setStatus(String status) { this.status = status; }
	public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
	public void setScreenName(String screenName) { this.screenName = screenName; }
	public void setActionName(String actionName) { this.actionName = actionName; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
