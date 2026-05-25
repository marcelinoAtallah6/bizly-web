import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { BusinessApiService } from 'src/app/services/business-api.service';

export interface MenuPermissionRouteRow {
  menuId: number;
  route: string;
  menuPath?: string | null;
  applicationId?: number | null;
  applicationName?: string | null;
}

export interface PermissionMetadataResponse {
  actions: string[];
  /** Menu id → verbs granted on UM_ROLE_MENU_PERM for that menu (JSON keys are strings). */
  actionsByMenuId?: Record<string, string[]>;
  menus: MenuPermissionRouteRow[];
}

@Injectable({ providedIn: 'root' })
export class UmMenuMetadataService {
  constructor(private readonly api: BusinessApiService) {}

  permissionMetadata(): Observable<PermissionMetadataResponse> {
    return this.api.postEnvelope<PermissionMetadataResponse>(
      GlobalConstants.API_ENDPOINTS.um.menu.permissionMetadata,
      {},
      'errors'
    );
  }
}
