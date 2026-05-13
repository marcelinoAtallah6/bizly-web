package com.settings.api.dto.query;

import java.time.Instant;
import java.util.List;

public class QueryDefResponse {

	private Long id;
	private String name;
	private String description;
	private String sqlText;
	private String parametersJson;
	private Instant createdAt;
	private Instant updatedAt;
	private String createdBy;

	private List<String> grantRoles;
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

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
}
