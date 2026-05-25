import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddTravelInvoiceRequest,
  GetsTravelInvoicesResponse,
  TravelPageRequest,
  UpdateTravelInvoiceRequest,
} from 'src/app/core/models/travel.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({ providedIn: 'root' })
export class TravelInvoiceService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: TravelPageRequest): Observable<GetsTravelInvoicesResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.invoice.gets, body);
  }

  add(body: AddTravelInvoiceRequest): Observable<{ id: number }> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.invoice.add, body, 'success-and-errors');
  }

  update(body: UpdateTravelInvoiceRequest): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.invoice.update, body, 'success-and-errors');
  }

  delete(body: { id: number }): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.invoice.delete, body, 'success-and-errors');
  }
}
