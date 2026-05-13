package com.bm.api.dto.notif;

/**
 * Pagination request for the inbox dropdown. Both fields are optional — when
 * the body is empty or the values are null/non-positive the service falls
 * back to {@code pageNumber=0, pageSize=15}.
 */
public class NotifInboxRecentRequest {

	private Integer pageNumber;
	private Integer pageSize;

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
}
