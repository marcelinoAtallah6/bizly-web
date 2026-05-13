package com.settings.api.dto.reporting;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.settings.api.dto.reporting.ReportFilterDef.FilterType;

/**
 * Persisted shape of one report filter (lives inside
 * {@code SettingsReport.filtersJson}). Same fields as the runtime
 * {@link ReportFilterDef} the UI uses, plus the binding info used by the
 * backend to map the filter value to the linked query's named parameter(s).
 *
 * <p>For most types one filter ↔ one named parameter. For {@code DATE_RANGE}
 * the filter binds two parameters: {@link #fromParamName} and
 * {@link #toParamName}.
 *
 * <p>Filters whose param names are missing from the linked SQL are simply
 * ignored at execution time, so the builder can declare optional filters
 * without breaking existing queries.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportFilterConfig {

	private String key;
	private String label;
	private FilterType type;
	private String placeholder;

	/** Bound param name (e.g. {@code username}). Ignored for {@code DATE_RANGE}. */
	private String paramName;
	/** Bound param name for the lower bound of {@code DATE_RANGE}. */
	private String fromParamName;
	/** Bound param name for the upper bound of {@code DATE_RANGE}. */
	private String toParamName;

	private List<ReportFilterDef.Option> options;

	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	public FilterType getType() { return type; }
	public void setType(FilterType type) { this.type = type; }
	public String getPlaceholder() { return placeholder; }
	public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
	public String getParamName() { return paramName; }
	public void setParamName(String paramName) { this.paramName = paramName; }
	public String getFromParamName() { return fromParamName; }
	public void setFromParamName(String fromParamName) { this.fromParamName = fromParamName; }
	public String getToParamName() { return toParamName; }
	public void setToParamName(String toParamName) { this.toParamName = toParamName; }
	public List<ReportFilterDef.Option> getOptions() { return options; }
	public void setOptions(List<ReportFilterDef.Option> options) { this.options = options; }
}
