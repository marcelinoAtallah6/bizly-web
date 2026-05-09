import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { BusinessApiService } from 'src/app/services/business-api.service';
import {
  AddProductRequest,
  AddProductResponse,
  DeleteProductRequest,
  DeleteProductResponse,
  GetProductRequest,
  GetProductResponse,
  GetsProductsRequest,
  GetsProductsResponse,
  UpdateProductRequest,
  UpdateProductResponse,
} from 'src/app/core/models/pm.models';

@Injectable({
  providedIn: 'root',
})
export class PmProductService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: GetsProductsRequest): Observable<GetsProductsResponse> {
    return this.api.postEnvelope<GetsProductsResponse>(GlobalConstants.API_ENDPOINTS.pm.product.gets, body);
  }

  get(body: GetProductRequest): Observable<GetProductResponse> {
    return this.api.postEnvelope<GetProductResponse>(GlobalConstants.API_ENDPOINTS.pm.product.get, body);
  }

  add(body: AddProductRequest): Observable<AddProductResponse> {
    return this.api.postEnvelope<AddProductResponse>(
      GlobalConstants.API_ENDPOINTS.pm.product.add,
      body,
      'success-and-errors'
    );
  }

  update(body: UpdateProductRequest): Observable<UpdateProductResponse> {
    return this.api.postEnvelope<UpdateProductResponse>(
      GlobalConstants.API_ENDPOINTS.pm.product.update,
      body,
      'success-and-errors'
    );
  }

  delete(body: DeleteProductRequest): Observable<DeleteProductResponse> {
    return this.api.postEnvelope<DeleteProductResponse>(
      GlobalConstants.API_ENDPOINTS.pm.product.delete,
      body,
      'success-and-errors'
    );
  }
}
