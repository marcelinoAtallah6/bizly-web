package com.settings.api.dto.reporting;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Metadata that describes a report type to the UI.
 *
 * <p>Returned by {@code POST /reporting/getTypes}. The frontend uses this to
 * render the report picker, build the filter form, render the grid header,
 * and pick the default sort.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportTypeMeta {

	/** Stable machine key (e.g. {@code AUDIT_LOG}). Sent back on every request. */
	private String key;
	private String name;
	private String description;
	/** Soft grouping for the picker — e.g. "Security", "Business". */
	private String category;
	private String icon;
	private List<ReportFilterDef> filters;
	private List<ReportColumnDef> columns;
	private String defaultSortKey;
	private String defaultSortDir;

	public ReportTypeMeta() {}

	public ReportTypeMeta(String key, String name, String description, String category, String icon) {
		this.key = key;
		this.name = name;
		this.description = description;
		this.category = category;
		this.icon = icon;
	}

	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }
	public String getIcon() { return icon; }
	public void setIcon(String icon) { this.icon = icon; }
	public List<ReportFilterDef> getFilters() { return filters; }
	public void setFilters(List<ReportFilterDef> filters) { this.filters = filters; }
	public List<ReportColumnDef> getColumns() { return columns; }
	public void setColumns(List<ReportColumnDef> columns) { this.columns = columns; }
	public String getDefaultSortKey() { return defaultSortKey; }
	public void setDefaultSortKey(String defaultSortKey) { this.defaultSortKey = defaultSortKey; }
	public String getDefaultSortDir() { return defaultSortDir; }
	public void setDefaultSortDir(String defaultSortDir) { this.defaultSortDir = defaultSortDir; }
}
