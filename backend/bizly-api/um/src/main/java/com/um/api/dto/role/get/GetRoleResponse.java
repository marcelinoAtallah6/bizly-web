package com.um.api.dto.role.get;

import java.time.LocalDateTime;

public class GetRoleResponse {

	private Long id;
	private String name;
	private Integer roleType;
	private Long roleLevelId;
	private String roleLevelCode;
	private String roleKind;
	private LocalDateTime createdAt;
	private Long parentRoleId;
	private Long businessId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getRoleType() {
		return roleType;
	}

	public void setRoleType(Integer roleType) {
		this.roleType = roleType;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Long getRoleLevelId() { return roleLevelId; }
	public void setRoleLevelId(Long roleLevelId) { this.roleLevelId = roleLevelId; }
	public String getRoleLevelCode() { return roleLevelCode; }
	public void setRoleLevelCode(String roleLevelCode) { this.roleLevelCode = roleLevelCode; }
	public String getRoleKind() { return roleKind; }
	public void setRoleKind(String roleKind) { this.roleKind = roleKind; }

	public Long getParentRoleId() {
		return parentRoleId;
	}

	public void setParentRoleId(Long parentRoleId) {
		this.parentRoleId = parentRoleId;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}
}