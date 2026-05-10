package com.settings.api.dto.query;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class QueryDefGetsRequest {

	@NotNull(message = "pageNumber is required")
	@Min(value = 0, message = "pageNumber must be >= 0")
	private Integer pageNumber = 0;

	@NotNull(message = "pageSize is required")
	@Min(value = 1, message = "pageSize must be >= 1")
	@Max(value = 500, message = "pageSize must be <= 500")
	private Integer pageSize = 15;

	/** Optional case-insensitive substring match on name (trimmed; empty = no filter). */
	private String nameSearch;

	public Integer getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public String getNameSearch() {
		return nameSearch;
	}

	public void setNameSearch(String nameSearch) {
		this.nameSearch = nameSearch;
	}
}
