package com.um.api.model.workflow;

import java.time.LocalDateTime;

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
@Table(name = DatabaseConstants.WORKFLOW_INSTANCE_TABLE, schema = DatabaseConstants.SCHEMA)
public class WorkflowInstance {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wf_inst_seq")
	@SequenceGenerator(name = "wf_inst_seq", sequenceName = DatabaseConstants.WORKFLOW_INSTANCE_SEQ, allocationSize = 1)
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

	@Column(name = "checker_comment", length = 4000)
	private String checkerComment;

	@Column(name = "endpoint_id")
	private Long endpointId;

	@Column(name = "captured_http_method", length = 16)
	private String capturedHttpMethod;

	@Column(name = "captured_http_path", length = 4000)
	private String capturedHttpPath;

	@Column(name = "captured_http_query", length = 4000)
	private String capturedHttpQuery;

	@Lob
	@Column(name = "captured_headers_json")
	private String capturedHeadersJson;

	@Lob
	@Column(name = "captured_body")
	private String capturedBody;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getWorkflowConfigId() {
		return workflowConfigId;
	}

	public void setWorkflowConfigId(Long workflowConfigId) {
		this.workflowConfigId = workflowConfigId;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	public Long getTriggeredByUserId() {
		return triggeredByUserId;
	}

	public void setTriggeredByUserId(Long triggeredByUserId) {
		this.triggeredByUserId = triggeredByUserId;
	}

	public String getTriggeredByUsername() {
		return triggeredByUsername;
	}

	public void setTriggeredByUsername(String triggeredByUsername) {
		this.triggeredByUsername = triggeredByUsername;
	}

	public Integer getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(Integer currentLevel) {
		this.currentLevel = currentLevel;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPayloadJson() {
		return payloadJson;
	}

	public void setPayloadJson(String payloadJson) {
		this.payloadJson = payloadJson;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Long getEndpointId() {
		return endpointId;
	}

	public void setEndpointId(Long endpointId) {
		this.endpointId = endpointId;
	}

	public String getCapturedHttpMethod() {
		return capturedHttpMethod;
	}

	public void setCapturedHttpMethod(String capturedHttpMethod) {
		this.capturedHttpMethod = capturedHttpMethod;
	}

	public String getCapturedHttpPath() {
		return capturedHttpPath;
	}

	public void setCapturedHttpPath(String capturedHttpPath) {
		this.capturedHttpPath = capturedHttpPath;
	}

	public String getCapturedHttpQuery() {
		return capturedHttpQuery;
	}

	public void setCapturedHttpQuery(String capturedHttpQuery) {
		this.capturedHttpQuery = capturedHttpQuery;
	}

	public String getCapturedHeadersJson() {
		return capturedHeadersJson;
	}

	public void setCapturedHeadersJson(String capturedHeadersJson) {
		this.capturedHeadersJson = capturedHeadersJson;
	}

	public String getCapturedBody() {
		return capturedBody;
	}

	public void setCapturedBody(String capturedBody) {
		this.capturedBody = capturedBody;
	}

	public String getCheckerComment() {
		return checkerComment;
	}

	public void setCheckerComment(String checkerComment) {
		this.checkerComment = checkerComment;
	}
}
