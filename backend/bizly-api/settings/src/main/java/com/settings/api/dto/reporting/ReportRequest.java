package com.settings.api.dto.reporting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * One request shape used by both {@code /reporting/generate} and
 * {@code /reporting/export} (the latter ignores pagination and respects only
 * sort + maxRows on the service side). Filters are an open map so each
 * report can declare its own filter schema without touching this DTO.
 */
public class ReportRequest {

	@NotBlank
	@Size(max = 64)
	private String typeKey;

	/** Free-form per-report filter values keyed by {@code ReportFilterDef.key}. */
	private Map<String, Object> filters;

	/** 0-based page index. Ignored by export. */
	private int pageNumber;
	/** Capped server-side to a maximum. */
	private int pageSize;
	private String sortBy;
	/** "ASC" or "DESC" — case-insensitive, defaulted by the service. */
	private String sortDir;

	/** Optional: only used by export, capped server-side. */
	private Integer maxRows;

	public ReportRequest() {}

	public String getTypeKey() { return typeKey; }
	public void setTypeKey(String typeKey) { this.typeKey = typeKey; }

	public Map<String, Object> getFilters() {
		return filters != null ? filters : Collections.emptyMap();
	}

	public void setFilters(Map<String, Object> filters) {
		this.filters = filters != null ? new HashMap<>(filters) : null;
	}

	public int getPageNumber() { return pageNumber; }
	public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
	public int getPageSize() { return pageSize; }
	public void setPageSize(int pageSize) { this.pageSize = pageSize; }
	public String getSortBy() { return sortBy; }
	public void setSortBy(String sortBy) { this.sortBy = sortBy; }
	public String getSortDir() { return sortDir; }
	public void setSortDir(String sortDir) { this.sortDir = sortDir; }
	public Integer getMaxRows() { return maxRows; }
	public void setMaxRows(Integer maxRows) { this.maxRows = maxRows; }
}
