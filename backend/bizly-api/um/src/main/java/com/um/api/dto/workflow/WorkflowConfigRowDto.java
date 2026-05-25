package com.um.api.dto.workflow;

import java.time.LocalDateTime;

public class WorkflowConfigRowDto {

	private Long id;
	private String screenName;
	private String actionName;
	private boolean hasWorkflow;
	private int levelCount;
	private String makerRolesJson;
	private String checkerRolesJson;
	private String makerUsersJson;
	private String checkerUsersJson;
	private String builtInKey;
	private Long businessId;
	private String createdBy;
	private LocalDateTime createdAt;
	/** When true, the row is a system template (e.g. business registration) and cannot be edited. */
	private boolean systemLocked;

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
		return hasWorkflow;
	}

	public void setHasWorkflow(boolean hasWorkflow) {
		this.hasWorkflow = hasWorkflow;
	}

	public int getLevelCount() {
		return levelCount;
	}

	public void setLevelCount(int levelCount) {
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

	public boolean isSystemLocked() {
		return systemLocked;
	}

	public void setSystemLocked(boolean systemLocked) {
		this.systemLocked = systemLocked;
	}
}
