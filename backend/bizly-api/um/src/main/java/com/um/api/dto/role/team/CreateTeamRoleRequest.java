package com.um.api.dto.role.team;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CreateTeamRoleRequest {

	@NotBlank
	@Size(max = 120)
	private String name;

	/** When set, the new role inherits from this parent (template or team role). Defaults to the sector template. */
	private Long parentRoleId;

	/** @deprecated Prefer {@link #parentRoleId}; when parent defaults to the sector template, optionally copy from another template. */
	private Long sourceTemplateRoleId;

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public Long getParentRoleId() { return parentRoleId; }
	public void setParentRoleId(Long parentRoleId) { this.parentRoleId = parentRoleId; }
	public Long getSourceTemplateRoleId() { return sourceTemplateRoleId; }
	public void setSourceTemplateRoleId(Long sourceTemplateRoleId) { this.sourceTemplateRoleId = sourceTemplateRoleId; }
}
