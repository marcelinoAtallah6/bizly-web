package com.um.common;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class PageRequest {

	@NotNull(message = ApiDefaultValdiation.PAGE_NUMBER)
	@Min(value = 0, message = ApiDefaultValdiation.PAGE_NUMBER)
	private int pageNumber = 0;

	@NotNull(message = ApiDefaultValdiation.PAGE_SIZE)
	@Min(value = 1, message = ApiDefaultValdiation.PAGE_SIZE)
	private int pageSize = 10;

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
}
