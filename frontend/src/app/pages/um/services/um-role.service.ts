import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddRoleRequest,
  AddRoleResponse,
  DeleteRoleRequest,
  DeleteRoleResponse,
  GetRoleRequest,
  GetRoleResponse,
  GetsRolesRequest,
  GetsRolesResponse,
  UpdateRoleRequest,
  UpdateRoleResponse,
} from 'src/app/core/models/um.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({
  providedIn: 'root',
})
export class UmRoleService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: GetsRolesRequest): Observable<GetsRolesResponse> {
    return this.api.postEnvelope<GetsRolesResponse>(GlobalConstants.API_ENDPOINTS.um.role.gets, body);
  }

  get(body: GetRoleRequest): Observable<GetRoleResponse> {
    return this.api.postEnvelope<GetRoleResponse>(GlobalConstants.API_ENDPOINTS.um.role.get, body);
  }

  add(body: AddRoleRequest): Observable<AddRoleResponse> {
    return this.api.postEnvelope<AddRoleResponse>(
      GlobalConstants.API_ENDPOINTS.um.role.add,
      body,
      'success-and-errors'
    );
  }

  update(body: UpdateRoleRequest): Observable<UpdateRoleResponse> {
    return this.api.postEnvelope<UpdateRoleResponse>(
      GlobalConstants.API_ENDPOINTS.um.role.update,
      body,
      'success-and-errors'
    );
  }

  delete(body: DeleteRoleRequest): Observable<DeleteRoleResponse> {
    return this.api.postEnvelope<DeleteRoleResponse>(
      GlobalConstants.API_ENDPOINTS.um.role.delete,
      body,
      'success-and-errors'
    );
  }
}
