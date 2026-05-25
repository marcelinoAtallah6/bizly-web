package com.um.api.dto.workflow;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Creates a tenant-scoped (or global, for super-admin) workflow definition for any screen route + action.
 */
public class CreateWorkflowConfigRequest {

	@NotBlank
	@Size(max = 200)
	private String screenName;

	@NotBlank
	@Size(max = 200)
	private String actionName;

	@NotNull
	private Boolean hasWorkflow;

	@NotNull
	@Min(1)
	@Max(10)
	private Integer levelCount;

	/** JSON array of role names; optional when {@link #makerUsersJson} pins makers by username. */
	private String makerRolesJson;

	/** JSON array of role names; optional when {@link #checkerUsersJson} pins checkers by username. */
	private String checkerRolesJson;

	private String makerUsersJson;

	private String checkerUsersJson;

	/**
	 * Optional business id when an admin provisions a global row; otherwise taken from
	 * {@link com.um.security.BusinessContextHolder}.
	 */
	private Long businessId;

	/**
	 * When set, {@code screenName} and {@code actionName} are taken from {@code UM_WORKFLOW_API_ENDPOINT}
	 * (active mutating row) and menu-permission catalog validation is skipped for that pair.
	 */
	private Long endpointId;

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

	public Boolean getHasWorkflow() {
		return hasWorkflow;
	}

	public void setHasWorkflow(Boolean hasWorkflow) {
		this.hasWorkflow = hasWorkflow;
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

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	public Long getEndpointId() {
		return endpointId;
	}

	public void setEndpointId(Long endpointId) {
		this.endpointId = endpointId;
	}
}
