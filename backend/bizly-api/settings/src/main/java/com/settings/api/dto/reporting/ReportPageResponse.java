package com.settings.api.dto.reporting;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Paginated report payload. {@code columns} mirrors the type's column defs
 * so the frontend can render the grid header even if a brand-new client
 * skipped {@code /reporting/getTypes}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportPageResponse {

	private String typeKey;
	private List<ReportColumnDef> columns;
	private List<Map<String, Object>> items;
	private long totalCount;
	private int pageNumber;
	private int pageSize;
	private int totalPages;
	/** Server timestamp of generation, ISO-8601 UTC. */
	private String generatedAt;

	public String getTypeKey() { return typeKey; }
	public void setTypeKey(String typeKey) { this.typeKey = typeKey; }
	public List<ReportColumnDef> getColumns() { return columns; }
	public void setColumns(List<ReportColumnDef> columns) { this.columns = columns; }
	public List<Map<String, Object>> getItems() { return items; }
	public void setItems(List<Map<String, Object>> items) { this.items = items; }
	public long getTotalCount() { return totalCount; }
	public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
	public int getPageNumber() { return pageNumber; }
	public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
	public int getPageSize() { return pageSize; }
	public void setPageSize(int pageSize) { this.pageSize = pageSize; }
	public int getTotalPages() { return totalPages; }
	public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
	public String getGeneratedAt() { return generatedAt; }
	public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
}
