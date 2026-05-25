package com.travel.api.service.followup;

import com.travel.api.dto.followup.TravelFollowUpDtos.*;
import com.travel.common.PageResponse;

public interface ITravelFollowUpService {

	AddTravelFollowUpResponse add(AddTravelFollowUpRequest request);

	UpdateTravelFollowUpResponse update(UpdateTravelFollowUpRequest request);

	DeleteTravelFollowUpResponse delete(DeleteTravelFollowUpRequest request);

	GetTravelFollowUpResponse get(GetTravelFollowUpRequest request);

	PageResponse<GetTravelFollowUpResponse> gets(GetsTravelFollowUpsRequest request);
}
