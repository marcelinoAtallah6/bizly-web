package com.travel.api.service.invoice;

import com.travel.api.dto.invoice.TravelInvoiceDtos.*;
import com.travel.common.PageResponse;

public interface ITravelInvoiceService {

	AddTravelInvoiceResponse add(AddTravelInvoiceRequest request);

	UpdateTravelInvoiceResponse update(UpdateTravelInvoiceRequest request);

	DeleteTravelInvoiceResponse delete(DeleteTravelInvoiceRequest request);

	GetTravelInvoiceResponse get(GetTravelInvoiceRequest request);

	PageResponse<GetTravelInvoiceResponse> gets(GetsTravelInvoicesRequest request);
}
