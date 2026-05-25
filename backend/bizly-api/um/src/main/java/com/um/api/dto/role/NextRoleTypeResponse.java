package com.um.api.dto.role;

/**
 * Next {@code UM_ROLE.ROLE_TYPE} value (max existing + 1) for portal role creation.
 */
public class NextRoleTypeResponse {

	private int nextRoleType;

	public int getNextRoleType() {
		return nextRoleType;
	}

	public void setNextRoleType(int nextRoleType) {
		this.nextRoleType = nextRoleType;
	}
}
