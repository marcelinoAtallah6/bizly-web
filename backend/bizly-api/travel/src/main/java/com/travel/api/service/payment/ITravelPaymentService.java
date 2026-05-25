package com.travel.api.service.payment;

import com.travel.api.dto.payment.TravelPaymentDtos.*;
import com.travel.common.PageResponse;

public interface ITravelPaymentService {

	AddTravelPaymentResponse add(AddTravelPaymentRequest request);

	UpdateTravelPaymentResponse update(UpdateTravelPaymentRequest request);

	DeleteTravelPaymentResponse delete(DeleteTravelPaymentRequest request);

	GetTravelPaymentResponse get(GetTravelPaymentRequest request);

	PageResponse<GetTravelPaymentResponse> gets(GetsTravelPaymentsRequest request);
}
