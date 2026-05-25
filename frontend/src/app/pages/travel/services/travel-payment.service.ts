import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddTravelPaymentRequest,
  GetsTravelPaymentsResponse,
  TravelPageRequest,
  UpdateTravelPaymentRequest,
} from 'src/app/core/models/travel.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({ providedIn: 'root' })
export class TravelPaymentService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: TravelPageRequest): Observable<GetsTravelPaymentsResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.payment.gets, body);
  }

  add(body: AddTravelPaymentRequest): Observable<{ id: number }> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.payment.add, body, 'success-and-errors');
  }

  update(body: UpdateTravelPaymentRequest): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.payment.update, body, 'success-and-errors');
  }

  delete(body: { id: number }): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.payment.delete, body, 'success-and-errors');
  }
}
