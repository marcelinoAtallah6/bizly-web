package com.travel.api.service.visa;

import com.travel.api.dto.visa.TravelVisaDtos.*;
import com.travel.common.PageResponse;

public interface ITravelVisaApplicationService {

	AddTravelVisaResponse add(AddTravelVisaRequest request);

	UpdateTravelVisaResponse update(UpdateTravelVisaRequest request);

	DeleteTravelVisaResponse delete(DeleteTravelVisaRequest request);

	GetTravelVisaResponse get(GetTravelVisaRequest request);

	PageResponse<GetTravelVisaResponse> gets(GetsTravelVisasRequest request);
}
