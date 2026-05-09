package com.um.api.dto.audit;

import com.um.common.PageRequest;

public class GetsAuditLogsRequest extends PageRequest {

	private String usernameContains;

	public String getUsernameContains() {
		return usernameContains;
	}

	public void setUsernameContains(String usernameContains) {
		this.usernameContains = usernameContains;
	}
}
