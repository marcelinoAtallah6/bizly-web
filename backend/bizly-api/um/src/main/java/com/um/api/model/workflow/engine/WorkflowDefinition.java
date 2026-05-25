package com.um.api.model.workflow.engine;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.um.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.WORKFLOW_DEFINITION_TABLE, schema = DatabaseConstants.SCHEMA)
public class WorkflowDefinition {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wf_def_seq")
	@SequenceGenerator(name = "wf_def_seq", sequenceName = DatabaseConstants.WORKFLOW_DEFINITION_SEQ, allocationSize = 1)
	private Long id;

	@Column(name = "action_code", nullable = false, length = 80)
	private String actionCode;

	@Column(name = "version_no", nullable = false)
	private Integer versionNo;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Column(name = "business_id")
	private Long businessId;

	@Column(name = "display_name", length = 200)
	private String displayName;

	@Column(name = "notes", length = 1000)
	private String notes;

	@Column(name = "published_at")
	private LocalDateTime publishedAt;

	@Column(name = "published_by", length = 120)
	private String publishedBy;

	@Column(name = "created_by", length = 120)
	private String createdBy;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "workflow_type", length = 40)
	private String workflowType = "ACTION_PIPELINE";

	@Column(name = "role_restricted")
	private boolean roleRestricted;

	@Column(name = "allow_child_role_inherit")
	private boolean allowChildRoleInherit;

	@Column(name = "role_codes_json")
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

	public LocalDateTime getPublishedAt() {
		return publishedAt;
	}

	public void setPublishedAt(LocalDateTime publishedAt) {
		this.publishedAt = publishedAt;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getPublishedBy() {
		return publishedBy;
	}

	public void setPublishedBy(String publishedBy) {
		this.publishedBy = publishedBy;
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

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
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
