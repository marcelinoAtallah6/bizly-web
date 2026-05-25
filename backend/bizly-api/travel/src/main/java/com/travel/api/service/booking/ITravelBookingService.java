package com.travel.api.service.booking;

import com.travel.api.dto.booking.TravelBookingDtos.*;
import com.travel.common.PageResponse;

public interface ITravelBookingService {

	AddTravelBookingResponse add(AddTravelBookingRequest request, String username);

	UpdateTravelBookingResponse update(UpdateTravelBookingRequest request, String username);

	DeleteTravelBookingResponse delete(DeleteTravelBookingRequest request);

	GetTravelBookingResponse get(GetTravelBookingRequest request);

	PageResponse<GetTravelBookingResponse> gets(GetsTravelBookingsRequest request);

	BookingAvailabilityResponse availability(BookingAvailabilityRequest request);
}
