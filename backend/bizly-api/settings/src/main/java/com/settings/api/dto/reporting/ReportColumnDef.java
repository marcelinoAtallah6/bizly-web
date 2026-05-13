package com.settings.api.dto.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Describes one column the report produces. The frontend uses this to build
 * the grid (header label, formatter), and only allows sorting on columns
 * where {@link #sortable} is true.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportColumnDef {

	public enum ColumnType {
		STRING,
		NUMBER,
		MONEY,
		DATE,
		DATETIME,
		BOOLEAN
	}

	private String key;
	private String label;
	private ColumnType type;
	private boolean sortable;

	public ReportColumnDef() {}

	public static ReportColumnDef of(String key, String label, ColumnType type, boolean sortable) {
		ReportColumnDef c = new ReportColumnDef();
		c.key = key;
		c.label = label;
		c.type = type;
		c.sortable = sortable;
		return c;
	}

	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	public ColumnType getType() { return type; }
	public void setType(ColumnType type) { this.type = type; }
	public boolean isSortable() { return sortable; }
	public void setSortable(boolean sortable) { this.sortable = sortable; }
}
