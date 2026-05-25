import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddTravelDestinationRequest,
  GetsTravelDestinationsResponse,
  TravelDestinationRow,
  TravelPageRequest,
  UpdateTravelDestinationRequest,
} from 'src/app/core/models/travel.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({ providedIn: 'root' })
export class TravelDestinationService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: TravelPageRequest): Observable<GetsTravelDestinationsResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.destination.gets, body);
  }

  get(body: { id: number }): Observable<TravelDestinationRow> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.destination.get, body);
  }

  add(body: AddTravelDestinationRequest): Observable<{ id: number }> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.destination.add, body, 'success-and-errors');
  }

  update(body: UpdateTravelDestinationRequest): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.destination.update, body, 'success-and-errors');
  }

  delete(body: { id: number }): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.destination.delete, body, 'success-and-errors');
  }
}
