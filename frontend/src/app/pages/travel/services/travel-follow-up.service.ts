import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddTravelFollowUpRequest,
  GetsTravelFollowUpsResponse,
  TravelPageRequest,
  UpdateTravelFollowUpRequest,
} from 'src/app/core/models/travel.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({ providedIn: 'root' })
export class TravelFollowUpService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: TravelPageRequest): Observable<GetsTravelFollowUpsResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.followUp.gets, body);
  }

  add(body: AddTravelFollowUpRequest): Observable<{ id: number }> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.followUp.add, body, 'success-and-errors');
  }

  update(body: UpdateTravelFollowUpRequest): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.followUp.update, body, 'success-and-errors');
  }

  delete(body: { id: number }): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.followUp.delete, body, 'success-and-errors');
  }
}
