import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddTravelPackageRequest,
  AddTravelPackageResponse,
  DeleteTravelPackageRequest,
  DeleteTravelPackageResponse,
  GetTravelPackageRequest,
  GetTravelPackageResponse,
  GetsTravelPackagesRequest,
  GetsTravelPackagesResponse,
  UpdateTravelPackageRequest,
  UpdateTravelPackageResponse,
} from 'src/app/core/models/travel.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({ providedIn: 'root' })
export class TravelPackageService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: GetsTravelPackagesRequest): Observable<GetsTravelPackagesResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.package.gets, body);
  }

  get(body: GetTravelPackageRequest): Observable<GetTravelPackageResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.package.get, body);
  }

  add(body: AddTravelPackageRequest): Observable<AddTravelPackageResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.package.add, body, 'success-and-errors');
  }

  update(body: UpdateTravelPackageRequest): Observable<UpdateTravelPackageResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.package.update, body, 'success-and-errors');
  }

  delete(body: DeleteTravelPackageRequest): Observable<DeleteTravelPackageResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.package.delete, body, 'success-and-errors');
  }
}
