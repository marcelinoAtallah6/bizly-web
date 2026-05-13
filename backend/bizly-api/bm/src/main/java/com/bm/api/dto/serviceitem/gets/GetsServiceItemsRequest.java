package com.bm.api.dto.serviceitem.gets;

import com.bm.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetsServiceItemsRequest extends PageRequest {

	/** When false (default), only active services are returned. */
	private Boolean includeInactive;
}
