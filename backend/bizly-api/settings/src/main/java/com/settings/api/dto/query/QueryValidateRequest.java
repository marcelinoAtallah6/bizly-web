package com.settings.api.dto.query;

import javax.validation.constraints.NotBlank;

public class QueryValidateRequest {

	@NotBlank
	private String sqlText;

	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}
}
