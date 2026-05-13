package com.settings.api.dto.reporting;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Full report definition returned by builder list/get endpoints. Carries the
 * already-deserialised filters and columns so the Angular form can bind to
 * structured arrays instead of re-parsing the JSON blob.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportBuilderItem {

	private Long id;
	private String code;
	private String name;
	private String description;
	private String icon;
	private String status;
	private Long queryDefId;
	private String queryDefName;
	private List<ReportFilterConfig> filters;
	private List<ReportColumnConfig> columns;
	private String defaultSortKey;
	private String defaultSortDir;
	private Integer sortOrder;
	private Instant createdAt;
	private Instant updatedAt;
	private String createdBy;
	private String updatedBy;

	/** Roles currently granted access. Empty when the report is global. */
	private List<String> grantRoles;

	/** Specific users currently granted access. Empty when the report is global. */
	private List<String> grantUsernames;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getIcon() { return icon; }
	public void setIcon(String icon) { this.icon = icon; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public Long getQueryDefId() { return queryDefId; }
	public void setQueryDefId(Long queryDefId) { this.queryDefId = queryDefId; }
	public String getQueryDefName() { return queryDefName; }
	public void setQueryDefName(String queryDefName) { this.queryDefName = queryDefName; }
	public List<ReportFilterConfig> getFilters() { return filters; }
	public void setFilters(List<ReportFilterConfig> filters) { this.filters = filters; }
	public List<ReportColumnConfig> getColumns() { return columns; }
	public void setColumns(List<ReportColumnConfig> columns) { this.columns = columns; }
	public String getDefaultSortKey() { return defaultSortKey; }
	public void setDefaultSortKey(String defaultSortKey) { this.defaultSortKey = defaultSortKey; }
	public String getDefaultSortDir() { return defaultSortDir; }
	public void setDefaultSortDir(String defaultSortDir) { this.defaultSortDir = defaultSortDir; }
	public Integer getSortOrder() { return sortOrder; }
	public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
	public Instant getCreatedAt() { return createdAt; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	public String getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
	public List<String> getGrantRoles() { return grantRoles; }
	public void setGrantRoles(List<String> grantRoles) { this.grantRoles = grantRoles; }
	public List<String> getGrantUsernames() { return grantUsernames; }
	public void setGrantUsernames(List<String> grantUsernames) { this.grantUsernames = grantUsernames; }
}
