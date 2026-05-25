package com.um.api.dto.role.team;

public class ParentRoleOptionResponse {

	private Long id;
	private String name;
	/** {@code TEMPLATE} or {@code TEAM}. */
	private String optionKind;

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

	public String getOptionKind() {
		return optionKind;
	}

	public void setOptionKind(String optionKind) {
		this.optionKind = optionKind;
	}
}
