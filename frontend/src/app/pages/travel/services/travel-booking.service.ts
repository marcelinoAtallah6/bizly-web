import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddTravelBookingRequest,
  AddTravelBookingResponse,
  DeleteTravelBookingRequest,
  DeleteTravelBookingResponse,
  GetTravelBookingRequest,
  GetTravelBookingResponse,
  GetsTravelBookingsRequest,
  GetsTravelBookingsResponse,
  TravelBookingAvailabilityRequest,
  TravelBookingAvailabilityResponse,
  UpdateTravelBookingRequest,
  UpdateTravelBookingResponse,
} from 'src/app/core/models/travel.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({ providedIn: 'root' })
export class TravelBookingService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: GetsTravelBookingsRequest): Observable<GetsTravelBookingsResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.booking.gets, body);
  }

  get(body: GetTravelBookingRequest): Observable<GetTravelBookingResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.booking.get, body);
  }

  add(body: AddTravelBookingRequest): Observable<AddTravelBookingResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.booking.add, body, 'success-and-errors');
  }

  update(body: UpdateTravelBookingRequest): Observable<UpdateTravelBookingResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.booking.update, body, 'success-and-errors');
  }

  delete(body: DeleteTravelBookingRequest): Observable<DeleteTravelBookingResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.booking.delete, body, 'success-and-errors');
  }

  availability(body: TravelBookingAvailabilityRequest): Observable<TravelBookingAvailabilityResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.booking.availability, body);
  }
}
