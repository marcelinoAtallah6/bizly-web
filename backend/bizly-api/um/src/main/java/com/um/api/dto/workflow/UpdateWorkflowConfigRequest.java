package com.um.api.dto.workflow;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UpdateWorkflowConfigRequest {

	@NotNull
	private Long id;

	private Boolean hasWorkflow;

	private Integer levelCount;

	@Size(max = 4000)
	private String makerRolesJson;

	@Size(max = 4000)
	private String checkerRolesJson;

	@Size(max = 4000)
	private String makerUsersJson;

	@Size(max = 4000)
	private String checkerUsersJson;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
}
