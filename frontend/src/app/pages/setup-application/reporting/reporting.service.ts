import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { PageResponse } from 'src/app/core/models/api.types';
import { BusinessApiService } from 'src/app/services/business-api.service';

import {
  ActiveReportRef,
  ReportBuilderItem,
  ReportBuilderSaveRequest,
  ReportPageResponse,
  ReportRequest,
  ReportTypeMeta,
  ReportTypesResponse,
} from './reporting.types';

/**
 * Thin wrapper over the reporting endpoints (runner + builder).
 *
 * The component layer holds the filter / paging state; this service only
 * knows how to talk HTTP. Builder admin calls are grouped under
 * `.builder.*` for readability.
 */
@Injectable({ providedIn: 'root' })
export class ReportingService {
  constructor(private readonly api: BusinessApiService) {}

  // ---- Runner ----

  getTypes(): Observable<ReportTypeMeta[]> {
    return new Observable<ReportTypeMeta[]>((subscriber) => {
      const sub = this.api
        .postEnvelope<ReportTypesResponse>(
          GlobalConstants.API_ENDPOINTS.settings.reporting.getTypes,
          {},
          'errors'
        )
        .subscribe({
          next: (resp) => {
            subscriber.next(resp?.items ?? []);
            subscriber.complete();
          },
          error: (err) => subscriber.error(err),
        });
      return () => sub.unsubscribe();
    });
  }

  generate(req: ReportRequest): Observable<ReportPageResponse> {
    return this.api.postEnvelope<ReportPageResponse>(
      GlobalConstants.API_ENDPOINTS.settings.reporting.generate,
      req,
      'errors'
    );
  }

  export(req: ReportRequest): Observable<ReportPageResponse> {
    return this.api.postEnvelope<ReportPageResponse>(
      GlobalConstants.API_ENDPOINTS.settings.reporting.export,
      req,
      'errors'
    );
  }

  // ---- Builder (admin) ----

  builderList(body: {
    pageNumber: number;
    pageSize: number;
    nameSearch?: string | null;
  }): Observable<PageResponse<ReportBuilderItem>> {
    return this.api.postEnvelope<PageResponse<ReportBuilderItem>>(
      GlobalConstants.API_ENDPOINTS.settings.reporting.builder.list,
      body,
      'errors'
    );
  }

  builderGet(id: number): Observable<ReportBuilderItem> {
    return this.api.postEnvelope<ReportBuilderItem>(
      GlobalConstants.API_ENDPOINTS.settings.reporting.builder.get,
      { id },
      'errors'
    );
  }

  builderSave(req: ReportBuilderSaveRequest): Observable<ReportBuilderItem> {
    return this.api.postEnvelope<ReportBuilderItem>(
      GlobalConstants.API_ENDPOINTS.settings.reporting.builder.save,
      req,
      'success-and-errors'
    );
  }

  builderDelete(id: number): Observable<void> {
    return this.api.postEnvelope<void>(
      GlobalConstants.API_ENDPOINTS.settings.reporting.builder.delete,
      { id },
      'success-and-errors'
    );
  }

  builderListActive(): Observable<ActiveReportRef[]> {
    return this.api.postEnvelope<ActiveReportRef[]>(
      GlobalConstants.API_ENDPOINTS.settings.reporting.builder.listActive,
      {},
      'silent'
    );
  }

  /**
   * Reports menu ({@code /reports}): ACTIVE reports assigned to the current user/roles only.
   */
  listAssignedReports(): Observable<ActiveReportRef[]> {
    return this.api.postEnvelope<ActiveReportRef[]>(
      GlobalConstants.API_ENDPOINTS.settings.reporting.viewer.list,
      {},
      'errors'
    );
  }
}
