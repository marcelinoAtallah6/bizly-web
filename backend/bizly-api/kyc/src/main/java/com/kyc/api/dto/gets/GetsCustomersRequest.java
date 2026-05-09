package com.kyc.api.dto.gets;

import javax.validation.constraints.Min;

public class GetsCustomersRequest {

	@Min(0)
	private int pageNumber = 0;

	@Min(1)
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
