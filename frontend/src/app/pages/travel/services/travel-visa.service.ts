import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddTravelVisaRequest,
  GetsTravelVisasResponse,
  TravelPageRequest,
  TravelVisaRow,
  UpdateTravelVisaRequest,
} from 'src/app/core/models/travel.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({ providedIn: 'root' })
export class TravelVisaService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: TravelPageRequest): Observable<GetsTravelVisasResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.visa.gets, body);
  }

  add(body: AddTravelVisaRequest): Observable<{ id: number }> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.visa.add, body, 'success-and-errors');
  }

  update(body: UpdateTravelVisaRequest): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.visa.update, body, 'success-and-errors');
  }

  delete(body: { id: number }): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.visa.delete, body, 'success-and-errors');
  }
}
