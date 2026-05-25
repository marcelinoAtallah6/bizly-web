package com.travel.common;

import javax.validation.constraints.Min;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequest {

	@Min(value = 0, message = ApiDefaultValdiation.PAGE_NUMBER)
	private int pageNumber = 0;

	@Min(value = 1, message = ApiDefaultValdiation.PAGE_SIZE)
	private int pageSize = 10;
}