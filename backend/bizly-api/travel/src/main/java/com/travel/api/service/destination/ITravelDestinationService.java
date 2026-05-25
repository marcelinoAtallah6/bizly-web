package com.travel.api.service.destination;

import com.travel.api.dto.destination.TravelDestinationDtos.*;
import com.travel.common.PageResponse;

public interface ITravelDestinationService {

	AddTravelDestinationResponse add(AddTravelDestinationRequest request);

	UpdateTravelDestinationResponse update(UpdateTravelDestinationRequest request);

	DeleteTravelDestinationResponse delete(DeleteTravelDestinationRequest request);

	GetTravelDestinationResponse get(GetTravelDestinationRequest request);

	PageResponse<GetTravelDestinationResponse> gets(GetsTravelDestinationsRequest request);
}
