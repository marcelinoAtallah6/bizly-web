package com.settings.api.dto.reporting;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Describes one filter that the frontend should render for a given report type.
 * The frontend uses {@link #type} to pick the right input widget; the backend
 * uses {@link #key} to read the value out of the {@code filters} map on the
 * request.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportFilterDef {

	/** Logical input shape. The frontend maps these to widgets 1:1. */
	public enum FilterType {
		/** Single ISO date (yyyy-MM-dd). */
		DATE,
		/** {from, to} ISO date pair. */
		DATE_RANGE,
		/** Free text — substring match. */
		TEXT,
		/** Dropdown — uses {@link #options}. */
		SELECT,
		/** Username text — autocomplete fed from UM_USER on the frontend. */
		USER,
		/** Numeric input. */
		NUMBER
	}

	private String key;
	private String label;
	private FilterType type;
	private String placeholder;
	private List<Option> options;

	public ReportFilterDef() {
	}

	public static ReportFilterDef of(String key, String label, FilterType type) {
		ReportFilterDef d = new ReportFilterDef();
		d.key = key;
		d.label = label;
		d.type = type;
		return d;
	}

	public ReportFilterDef withPlaceholder(String placeholder) {
		this.placeholder = placeholder;
		return this;
	}

	public ReportFilterDef withOptions(List<Option> options) {
		this.options = options;
		return this;
	}

	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	public FilterType getType() { return type; }
	public void setType(FilterType type) { this.type = type; }
	public String getPlaceholder() { return placeholder; }
	public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
	public List<Option> getOptions() { return options; }
	public void setOptions(List<Option> options) { this.options = options; }

	public static class Option {
		private String value;
		private String label;

		public Option() {}
		public Option(String value, String label) {
			this.value = value;
			this.label = label;
		}
		public String getValue() { return value; }
		public void setValue(String value) { this.value = value; }
		public String getLabel() { return label; }
		public void setLabel(String label) { this.label = label; }
	}
}
