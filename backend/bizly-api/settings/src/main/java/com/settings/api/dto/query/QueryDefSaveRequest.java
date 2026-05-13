package com.settings.api.dto.query;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class QueryDefSaveRequest {

	private Long id;

	@NotBlank
	@Size(max = 200)
	private String name;

	@Size(max = 500)
	private String description;

	@NotBlank
	private String sqlText;

	private String parametersJson;

	/** Role-based visibility — see {@code ReportBuilderSaveRequest} for the same pattern. */
	private List<String> grantRoles;

	/** User-based visibility — empty + empty role grants = global to the tenant. */
	private List<String> grantUsernames;

	public List<String> getGrantRoles() { return grantRoles; }
	public void setGrantRoles(List<String> grantRoles) { this.grantRoles = grantRoles; }
	public List<String> getGrantUsernames() { return grantUsernames; }
	public void setGrantUsernames(List<String> grantUsernames) { this.grantUsernames = grantUsernames; }

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}

	public String getParametersJson() {
		return parametersJson;
	}

	public void setParametersJson(String parametersJson) {
		this.parametersJson = parametersJson;
	}
}
