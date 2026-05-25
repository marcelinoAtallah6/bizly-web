package com.travel.api.service.document;

import com.travel.api.dto.document.TravelDocumentDtos.*;
import com.travel.common.PageResponse;

public interface ITravelDocumentService {

	AddTravelDocumentResponse add(AddTravelDocumentRequest request, String username);

	UpdateTravelDocumentResponse update(UpdateTravelDocumentRequest request, String username);

	DeleteTravelDocumentResponse delete(DeleteTravelDocumentRequest request);

	GetTravelDocumentResponse get(GetTravelDocumentRequest request);

	PageResponse<GetTravelDocumentResponse> gets(GetsTravelDocumentsRequest request);
}
