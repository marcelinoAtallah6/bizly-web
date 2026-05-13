package com.settings.api.dto.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.settings.api.dto.reporting.ReportColumnDef.ColumnType;

/**
 * Persisted shape of one report column (lives inside
 * {@code SettingsReport.columnsJson}). Auto-introspected from the linked
 * query at save time and editable from the builder.
 *
 * <p>{@link #visible} controls the default visibility; the UI also exposes
 * a per-user toggle but doesn't persist it (per the requirement to keep
 * column-visibility a session preference).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportColumnConfig {

	/** Result-set column name as returned by the linked SQL (case as Oracle reports it). */
	private String key;
	/** Display label — defaults to the key in title-case when blank. */
	private String label;
	private ColumnType type;
	private boolean sortable;
	private boolean visible;

	public ReportColumnConfig() {}

	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	public ColumnType getType() { return type; }
	public void setType(ColumnType type) { this.type = type; }
	public boolean isSortable() { return sortable; }
	public void setSortable(boolean sortable) { this.sortable = sortable; }
	public boolean isVisible() { return visible; }
	public void setVisible(boolean visible) { this.visible = visible; }
}
