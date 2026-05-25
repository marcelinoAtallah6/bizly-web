import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddTravelClientRequest,
  AddTravelClientResponse,
  DeleteTravelClientRequest,
  DeleteTravelClientResponse,
  GetTravelClientRequest,
  GetTravelClientResponse,
  GetsTravelClientsRequest,
  GetsTravelClientsResponse,
  UpdateTravelClientRequest,
  UpdateTravelClientResponse,
} from 'src/app/core/models/travel.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({ providedIn: 'root' })
export class TravelClientService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: GetsTravelClientsRequest): Observable<GetsTravelClientsResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.client.gets, body);
  }

  get(body: GetTravelClientRequest): Observable<GetTravelClientResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.client.get, body);
  }

  add(body: AddTravelClientRequest): Observable<AddTravelClientResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.client.add, body, 'success-and-errors');
  }

  update(body: UpdateTravelClientRequest): Observable<UpdateTravelClientResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.client.update, body, 'success-and-errors');
  }

  delete(body: DeleteTravelClientRequest): Observable<DeleteTravelClientResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.client.delete, body, 'success-and-errors');
  }
}
