import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { BusinessApiService } from 'src/app/services/business-api.service';
import {
  AddCustomerRequest,
  AddCustomerResponse,
  DeleteCustomerRequest,
  DeleteCustomerResponse,
  GetCustomerRequest,
  GetCustomerResponse,
  GetsCustomersRequest,
  GetsCustomersResponse,
  UpdateCustomerRequest,
  UpdateCustomerResponse,
} from 'src/app/core/models/kyc.models';

@Injectable({
  providedIn: 'root',
})
export class KycCustomerService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: GetsCustomersRequest): Observable<GetsCustomersResponse> {
    return this.api.postEnvelope<GetsCustomersResponse>(
      GlobalConstants.API_ENDPOINTS.kyc.customer.gets,
      body
    );
  }

  get(body: GetCustomerRequest): Observable<GetCustomerResponse> {
    return this.api.postEnvelope<GetCustomerResponse>(GlobalConstants.API_ENDPOINTS.kyc.customer.get, body);
  }

  add(body: AddCustomerRequest): Observable<AddCustomerResponse> {
    return this.api.postEnvelope<AddCustomerResponse>(
      GlobalConstants.API_ENDPOINTS.kyc.customer.add,
      body,
      'success-and-errors'
    );
  }

  update(body: UpdateCustomerRequest): Observable<UpdateCustomerResponse> {
    return this.api.postEnvelope<UpdateCustomerResponse>(
      GlobalConstants.API_ENDPOINTS.kyc.customer.update,
      body,
      'success-and-errors'
    );
  }

  delete(body: DeleteCustomerRequest): Observable<DeleteCustomerResponse> {
    return this.api.postEnvelope<DeleteCustomerResponse>(
      GlobalConstants.API_ENDPOINTS.kyc.customer.delete,
      body,
      'success-and-errors'
    );
  }
}
