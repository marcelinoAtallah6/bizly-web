package com.um.api.dto.workflow.engine;

import java.time.LocalDateTime;

public class WorkflowDefinitionRowDto {

	private Long id;
	private String actionCode;
	private String actionDisplayName;
	private Integer versionNo;
	private String status;
	private Long businessId;
	private String displayName;
	private String notes;
	private LocalDateTime publishedAt;
	private String createdBy;
	private LocalDateTime createdAt;
	private int stepCount;
	private String workflowType;
	private boolean roleRestricted;
	private boolean allowChildRoleInherit;
	private String roleCodesJson;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public String getActionDisplayName() {
		return actionDisplayName;
	}

	public void setActionDisplayName(String actionDisplayName) {
		this.actionDisplayName = actionDisplayName;
	}

	public Integer getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(Integer versionNo) {
		this.versionNo = versionNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public LocalDateTime getPublishedAt() {
		return publishedAt;
	}

	public void setPublishedAt(LocalDateTime publishedAt) {
		this.publishedAt = publishedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public int getStepCount() {
		return stepCount;
	}

	public void setStepCount(int stepCount) {
		this.stepCount = stepCount;
	}

	public String getWorkflowType() {
		return workflowType;
	}

	public void setWorkflowType(String workflowType) {
		this.workflowType = workflowType;
	}

	public boolean isRoleRestricted() {
		return roleRestricted;
	}

	public void setRoleRestricted(boolean roleRestricted) {
		this.roleRestricted = roleRestricted;
	}

	public boolean isAllowChildRoleInherit() {
		return allowChildRoleInherit;
	}

	public void setAllowChildRoleInherit(boolean allowChildRoleInherit) {
		this.allowChildRoleInherit = allowChildRoleInherit;
	}

	public String getRoleCodesJson() {
		return roleCodesJson;
	}

	public void setRoleCodesJson(String roleCodesJson) {
		this.roleCodesJson = roleCodesJson;
	}
}
