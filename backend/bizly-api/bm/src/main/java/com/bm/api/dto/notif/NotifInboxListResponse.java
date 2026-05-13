package com.bm.api.dto.notif;

import java.util.List;

public class NotifInboxListResponse {

	private List<NotifInboxItemResponse> items;
	private long unreadCount;
	private long totalCount;
	private int pageNumber;
	private int pageSize;
	private boolean hasMore;

	public List<NotifInboxItemResponse> getItems() {
		return items;
	}

	public void setItems(List<NotifInboxItemResponse> items) {
		this.items = items;
	}

	public long getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(long unreadCount) {
		this.unreadCount = unreadCount;
	}

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

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

	public boolean isHasMore() {
		return hasMore;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}
}
