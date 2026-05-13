package com.bm.api.service.serviceitem;

import com.bm.api.dto.serviceitem.add.AddServiceItemRequest;
import com.bm.api.dto.serviceitem.add.AddServiceItemResponse;
import com.bm.api.dto.serviceitem.delete.DeactivateServiceItemRequest;
import com.bm.api.dto.serviceitem.delete.DeactivateServiceItemResponse;
import com.bm.api.dto.serviceitem.get.GetServiceItemRequest;
import com.bm.api.dto.serviceitem.get.GetServiceItemResponse;
import com.bm.api.dto.serviceitem.gets.GetsServiceItemsRequest;
import com.bm.api.dto.serviceitem.update.UpdateServiceItemRequest;
import com.bm.api.dto.serviceitem.update.UpdateServiceItemResponse;
import com.bm.common.PageResponse;

public interface IServiceItemService {

	AddServiceItemResponse add(AddServiceItemRequest request, String username);

	UpdateServiceItemResponse update(UpdateServiceItemRequest request, String username);

	DeactivateServiceItemResponse deactivate(DeactivateServiceItemRequest request, String username);

	GetServiceItemResponse get(GetServiceItemRequest request);

	PageResponse<GetServiceItemResponse> gets(GetsServiceItemsRequest request);
}
