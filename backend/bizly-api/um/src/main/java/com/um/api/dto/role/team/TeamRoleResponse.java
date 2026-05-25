package com.um.api.dto.role.team;

public class TeamRoleResponse {
	private Long id;
	private String name;
	private Long parentRoleId;
	private Long businessId;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public Long getParentRoleId() { return parentRoleId; }
	public void setParentRoleId(Long parentRoleId) { this.parentRoleId = parentRoleId; }
	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
}
