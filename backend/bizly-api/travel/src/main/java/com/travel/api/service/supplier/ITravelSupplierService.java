package com.travel.api.service.supplier;

import com.travel.api.dto.supplier.TravelSupplierDtos.*;
import com.travel.common.PageResponse;

public interface ITravelSupplierService {

	AddTravelSupplierResponse add(AddTravelSupplierRequest request);

	UpdateTravelSupplierResponse update(UpdateTravelSupplierRequest request);

	DeleteTravelSupplierResponse delete(DeleteTravelSupplierRequest request);

	GetTravelSupplierResponse get(GetTravelSupplierRequest request);

	PageResponse<GetTravelSupplierResponse> gets(GetsTravelSuppliersRequest request);
}
