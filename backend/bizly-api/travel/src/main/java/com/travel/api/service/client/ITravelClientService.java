package com.travel.api.service.client;

import com.travel.api.dto.client.TravelClientDtos.*;
import com.travel.common.PageResponse;

public interface ITravelClientService {

	AddTravelClientResponse add(AddTravelClientRequest request, String username);

	UpdateTravelClientResponse update(UpdateTravelClientRequest request, String username);

	DeleteTravelClientResponse delete(DeleteTravelClientRequest request);

	GetTravelClientResponse get(GetTravelClientRequest request);

	PageResponse<GetTravelClientResponse> gets(GetsTravelClientsRequest request);
}
