import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddServiceItemRequest,
  AddServiceItemResponse,
  DeactivateServiceItemRequest,
  DeactivateServiceItemResponse,
  GetServiceItemRequest,
  GetServiceItemResponse,
  GetsServiceItemsRequest,
  GetsServiceItemsResponse,
  UpdateServiceItemRequest,
  UpdateServiceItemResponse,
} from 'src/app/core/models/bm.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({
  providedIn: 'root',
})
export class BmServiceItemService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: GetsServiceItemsRequest): Observable<GetsServiceItemsResponse> {
    return this.api.postEnvelope<GetsServiceItemsResponse>(
      GlobalConstants.API_ENDPOINTS.bm.serviceItem.gets,
      body
    );
  }

  get(body: GetServiceItemRequest): Observable<GetServiceItemResponse> {
    return this.api.postEnvelope<GetServiceItemResponse>(
      GlobalConstants.API_ENDPOINTS.bm.serviceItem.get,
      body
    );
  }

  add(body: AddServiceItemRequest): Observable<AddServiceItemResponse> {
    return this.api.postEnvelope<AddServiceItemResponse>(
      GlobalConstants.API_ENDPOINTS.bm.serviceItem.add,
      body,
      'success-and-errors'
    );
  }

  update(body: UpdateServiceItemRequest): Observable<UpdateServiceItemResponse> {
    return this.api.postEnvelope<UpdateServiceItemResponse>(
      GlobalConstants.API_ENDPOINTS.bm.serviceItem.update,
      body,
      'success-and-errors'
    );
  }

  deactivate(body: DeactivateServiceItemRequest): Observable<DeactivateServiceItemResponse> {
    return this.api.postEnvelope<DeactivateServiceItemResponse>(
      GlobalConstants.API_ENDPOINTS.bm.serviceItem.deactivate,
      body,
      'success-and-errors'
    );
  }
}
