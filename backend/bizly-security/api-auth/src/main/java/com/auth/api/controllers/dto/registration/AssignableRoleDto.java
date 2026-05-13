package com.auth.api.controllers.dto.registration;

/**
 * Compact view of a role that the registration UI is allowed to display.
 * Restricted/admin-level roles are filtered out server-side before any list
 * leaves the JVM — they never appear in this DTO.
 */
public class AssignableRoleDto {

	private Long id;
	private String name;
	private String levelCode;
	private Boolean isDefault;

	public AssignableRoleDto() {}

	public AssignableRoleDto(Long id, String name, String levelCode, Boolean isDefault) {
		this.id = id;
		this.name = name;
		this.levelCode = levelCode;
		this.isDefault = isDefault;
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getLevelCode() { return levelCode; }
	public void setLevelCode(String levelCode) { this.levelCode = levelCode; }
	public Boolean getIsDefault() { return isDefault; }
	public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
