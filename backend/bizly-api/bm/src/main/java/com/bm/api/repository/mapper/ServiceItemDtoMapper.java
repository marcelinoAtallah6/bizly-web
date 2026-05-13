package com.bm.api.repository.mapper;

import com.bm.api.dto.serviceitem.get.GetServiceItemResponse;
import com.bm.api.model.ServiceItem;

public final class ServiceItemDtoMapper {

	private ServiceItemDtoMapper() {
	}

	public static GetServiceItemResponse toResponse(ServiceItem e) {
		GetServiceItemResponse r = new GetServiceItemResponse();
		r.setId(e.getId());
		r.setName(e.getName());
		r.setDescription(e.getDescription());
		r.setPrice(e.getPrice());
		r.setDurationMinutes(e.getDurationMinutes());
		r.setActive(e.getActive());
		r.setCreatedAt(e.getCreatedAt());
		r.setUpdatedAt(e.getUpdatedAt());
		r.setCreatedBy(e.getCreatedBy());
		r.setUpdatedBy(e.getUpdatedBy());
		return r;
	}
}
