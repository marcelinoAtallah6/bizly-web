package com.travel.api.service.packagepkg;

import com.travel.api.dto.packagepkg.TravelPackageDtos.*;
import com.travel.common.PageResponse;

public interface ITravelPackageService {

	AddTravelPackageResponse add(AddTravelPackageRequest request, String username);

	UpdateTravelPackageResponse update(UpdateTravelPackageRequest request, String username);

	DeleteTravelPackageResponse delete(DeleteTravelPackageRequest request);

	GetTravelPackageResponse get(GetTravelPackageRequest request);

	PageResponse<GetTravelPackageResponse> gets(GetsTravelPackagesRequest request);
}
