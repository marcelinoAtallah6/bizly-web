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
@Table(name = DatabaseConstants.WORKFLOW_CONFIG_TABLE, schema = DatabaseConstants.SCHEMA)
public class WorkflowConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wf_cfg_seq")
	@SequenceGenerator(name = "wf_cfg_seq", sequenceName = DatabaseConstants.WORKFLOW_CONFIG_SEQ, allocationSize = 1)
	private Long id;

	@Column(name = "screen_name", nullable = false, length = 200)
	private String screenName;

	@Column(name = "action_name", nullable = false, length = 200)
	private String actionName;

	@Column(name = "has_workflow", nullable = false)
	private Integer hasWorkflow;

	@Column(name = "level_count", nullable = false)
	private Integer levelCount;

	@Lob
	@Column(name = "maker_roles_json")
	private String makerRolesJson;

	@Lob
	@Column(name = "checker_roles_json")
	private String checkerRolesJson;

	@Lob
	@Column(name = "maker_users_json")
	private String makerUsersJson;

	@Lob
	@Column(name = "checker_users_json")
	private String checkerUsersJson;

	/** System workflow key, e.g. {@code BUSINESS_REGISTRATION}; {@code NULL} for custom rows. */
	@Column(name = "built_in_key", length = 80)
	private String builtInKey;

	@Column(name = "business_id")
	private Long businessId;

	@Column(name = "created_by", length = 120)
	private String createdBy;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public boolean isHasWorkflow() {
		return hasWorkflow != null && hasWorkflow == 1;
	}

	public void setHasWorkflow(boolean v) {
		this.hasWorkflow = v ? 1 : 0;
	}

	public Integer getLevelCount() {
		return levelCount;
	}

	public void setLevelCount(Integer levelCount) {
		this.levelCount = levelCount;
	}

	public String getMakerRolesJson() {
		return makerRolesJson;
	}

	public void setMakerRolesJson(String makerRolesJson) {
		this.makerRolesJson = makerRolesJson;
	}

	public String getCheckerRolesJson() {
		return checkerRolesJson;
	}

	public void setCheckerRolesJson(String checkerRolesJson) {
		this.checkerRolesJson = checkerRolesJson;
	}

	public String getMakerUsersJson() {
		return makerUsersJson;
	}

	public void setMakerUsersJson(String makerUsersJson) {
		this.makerUsersJson = makerUsersJson;
	}

	public String getCheckerUsersJson() {
		return checkerUsersJson;
	}

	public void setCheckerUsersJson(String checkerUsersJson) {
		this.checkerUsersJson = checkerUsersJson;
	}

	public String getBuiltInKey() {
		return builtInKey;
	}

	public void setBuiltInKey(String builtInKey) {
		this.builtInKey = builtInKey;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
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
}
