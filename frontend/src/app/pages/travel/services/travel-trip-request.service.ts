import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddTravelTripRequestRequest,
  GetsTravelTripRequestsResponse,
  TravelPageRequest,
  TravelTripRequestRow,
  UpdateTravelTripRequestRequest,
} from 'src/app/core/models/travel.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({ providedIn: 'root' })
export class TravelTripRequestService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: TravelPageRequest): Observable<GetsTravelTripRequestsResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.tripRequest.gets, body);
  }

  get(body: { id: number }): Observable<TravelTripRequestRow> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.tripRequest.get, body);
  }

  add(body: AddTravelTripRequestRequest): Observable<{ id: number }> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.tripRequest.add, body, 'success-and-errors');
  }

  update(body: UpdateTravelTripRequestRequest): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.tripRequest.update, body, 'success-and-errors');
  }

  delete(body: { id: number }): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.tripRequest.delete, body, 'success-and-errors');
  }

  quote(body: { id: number; quotedAmount: number }): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.tripRequest.quote, body, 'success-and-errors');
  }

  accept(body: { id: number }): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.tripRequest.accept, body, 'success-and-errors');
  }

  reject(body: { id: number }): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.tripRequest.reject, body, 'success-and-errors');
  }

  convert(body: { id: number }): Observable<{ bookingId: number }> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.tripRequest.convert, body, 'success-and-errors');
  }
}
