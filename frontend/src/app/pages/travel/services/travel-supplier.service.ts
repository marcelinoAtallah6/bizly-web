import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddTravelSupplierRequest,
  GetsTravelSuppliersResponse,
  TravelPageRequest,
  UpdateTravelSupplierRequest,
} from 'src/app/core/models/travel.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({ providedIn: 'root' })
export class TravelSupplierService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: TravelPageRequest): Observable<GetsTravelSuppliersResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.supplier.gets, body);
  }

  add(body: AddTravelSupplierRequest): Observable<{ id: number }> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.supplier.add, body, 'success-and-errors');
  }

  update(body: UpdateTravelSupplierRequest): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.supplier.update, body, 'success-and-errors');
  }

  delete(body: { id: number }): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.supplier.delete, body, 'success-and-errors');
  }
}
