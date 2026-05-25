import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  CreateTeamRoleRequest,
  DeleteRoleRequest,
  ParentRoleOptionResponse,
  TeamRoleResponse,
} from 'src/app/core/models/um.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({
  providedIn: 'root',
})
export class UmTeamRoleService {
  constructor(private readonly api: BusinessApiService) {}

  list(): Observable<TeamRoleResponse[]> {
    return this.api.postEnvelope<TeamRoleResponse[]>(
      GlobalConstants.API_ENDPOINTS.um.teamRole.list,
      {}
    );
  }

  listAssignable(): Observable<TeamRoleResponse[]> {
    return this.api.postEnvelope<TeamRoleResponse[]>(
      GlobalConstants.API_ENDPOINTS.um.teamRole.listAssignable,
      {}
    );
  }

  parentOptions(): Observable<ParentRoleOptionResponse[]> {
    return this.api.postEnvelope<ParentRoleOptionResponse[]>(
      GlobalConstants.API_ENDPOINTS.um.teamRole.parentOptions,
      {}
    );
  }

  add(body: CreateTeamRoleRequest): Observable<TeamRoleResponse> {
    return this.api.postEnvelope<TeamRoleResponse>(
      GlobalConstants.API_ENDPOINTS.um.teamRole.add,
      body,
      'success-and-errors'
    );
  }

  delete(body: DeleteRoleRequest): Observable<string> {
    return this.api.postEnvelope<string>(
      GlobalConstants.API_ENDPOINTS.um.teamRole.delete,
      body,
      'success-and-errors'
    );
  }
}
