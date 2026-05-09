package com.kyc.common;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageResponse<T> {

	private List<T> items;

	private long totalCount;

	private int pageNumber;

	private int pageSize;

	private int totalPages;
}