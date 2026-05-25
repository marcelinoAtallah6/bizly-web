import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { BusinessApiService } from 'src/app/services/business-api.service';

export interface CatalogMenuDto {
  id?: number;
  applicationId?: number;
  parentId?: number | null;
  name: string;
  route?: string | null;
  icon?: string | null;
  isActive?: boolean;
  allowedRoles?: string | null;
  sortOrder?: number;
  children?: CatalogMenuDto[];
}

export interface CatalogApplicationDto {
  id?: number;
  name: string;
  description?: string | null;
  icon?: string | null;
  route?: string | null;
  isActive?: boolean;
  allowedRoles?: string | null;
  sortOrder?: number;
  menus?: CatalogMenuDto[];
}

export interface ApplicationCatalogResponse {
  applications: CatalogApplicationDto[];
}

export interface SaveApplicationCatalogRequest {
  id?: number | null;
  name: string;
  description?: string | null;
  icon?: string | null;
  route?: string | null;
  isActive?: boolean;
  allowedRoles?: string | null;
  sortOrder?: number | null;
}

export interface SaveMenuCatalogRequest {
  id?: number | null;
  applicationId: number;
  parentId?: number | null;
  name: string;
  route?: string | null;
  icon?: string | null;
  isActive?: boolean;
  allowedRoles?: string | null;
  sortOrder?: number | null;
}

@Injectable({ providedIn: 'root' })
export class ApplicationCatalogService {
  private readonly ep = GlobalConstants.API_ENDPOINTS.um.applicationCatalog;

  constructor(private readonly api: BusinessApiService) {}

  loadCatalog(): Observable<ApplicationCatalogResponse> {
    return this.api.postEnvelope<ApplicationCatalogResponse>(this.ep.catalog, {});
  }

  saveApplication(body: SaveApplicationCatalogRequest): Observable<{ id: number }> {
    return this.api.postEnvelope<{ id: number }>(this.ep.applicationSave, body, 'success-and-errors');
  }

  saveMenu(body: SaveMenuCatalogRequest): Observable<{ id: number }> {
    return this.api.postEnvelope<{ id: number }>(this.ep.menuSave, body, 'success-and-errors');
  }

  deleteApplication(id: number): Observable<void> {
    return this.api.postEnvelope<void>(this.ep.applicationDelete, { id }, 'success-and-errors');
  }

  deleteMenu(id: number): Observable<void> {
    return this.api.postEnvelope<void>(this.ep.menuDelete, { id }, 'success-and-errors');
  }

  reorderApplications(orderedIds: number[]): Observable<void> {
    return this.api.postEnvelope<void>(this.ep.reorder, { scope: 'APPLICATION', orderedIds }, 'success-and-errors');
  }

  reorderMenus(applicationId: number, orderedIds: number[], parentId?: number | null): Observable<void> {
    const body: Record<string, unknown> = { scope: 'MENU', applicationId, orderedIds };
    if (parentId != null) {
      body['parentId'] = parentId;
    }
    return this.api.postEnvelope<void>(this.ep.reorder, body, 'success-and-errors');
  }
}
