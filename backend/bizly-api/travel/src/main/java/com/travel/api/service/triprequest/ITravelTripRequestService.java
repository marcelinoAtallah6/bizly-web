package com.travel.api.service.triprequest;

import com.travel.api.dto.triprequest.TravelTripRequestDtos.*;
import com.travel.common.PageResponse;

public interface ITravelTripRequestService {

	AddTravelTripRequestResponse add(AddTravelTripRequestRequest request);

	UpdateTravelTripRequestResponse update(UpdateTravelTripRequestRequest request);

	DeleteTravelTripRequestResponse delete(DeleteTravelTripRequestRequest request);

	GetTravelTripRequestResponse get(GetTravelTripRequestRequest request);

	PageResponse<GetTravelTripRequestResponse> gets(GetsTravelTripRequestsRequest request);

	QuoteTravelTripRequestResponse quote(QuoteTravelTripRequestRequest request);

	AcceptTravelTripRequestResponse accept(AcceptTravelTripRequestRequest request);

	RejectTravelTripRequestResponse reject(RejectTravelTripRequestRequest request);

	ConvertTravelTripRequestResponse convert(ConvertTravelTripRequestRequest request, String username);
}
