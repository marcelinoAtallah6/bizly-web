import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { GlobalConstants } from '../common/GlobalConstants';
import { PageResponse } from '../core/models/api.types';
import {
  DashboardDetailDto,
  DashboardSaveDto,
  DashboardSummaryDto,
  QueryDefDto,
} from '../core/models/settings.models';
import { BusinessApiService } from './business-api.service';

@Injectable({ providedIn: 'root' })
export class SettingsApiService {
  constructor(private readonly api: BusinessApiService) {}

  async dashboardSummariesForUser(): Promise<DashboardSummaryDto[]> {
    const x = await firstValueFrom(
      this.api.postEnvelope<DashboardSummaryDto[]>(
        GlobalConstants.API_ENDPOINTS.settings.dashboardRuntime.forUser,
        {},
        'silent'
      )
    );
    return x ?? [];
  }

  async loadDashboard(body: { id?: number; slug?: string }): Promise<DashboardDetailDto | null> {
    try {
      return await firstValueFrom(
        this.api.postEnvelope<DashboardDetailDto>(
          GlobalConstants.API_ENDPOINTS.settings.dashboardRuntime.load,
          body,
          'silent'
        )
      );
    } catch {
      return null;
    }
  }

  /**
   * Fetches the dashboard the user opened last. Returns `null` when no preference is stored or the
   * saved dashboard is no longer reachable (server already gates by access).
   */
  async getLastDashboardId(): Promise<number | null> {
    try {
      const res = await firstValueFrom(
        this.api.postEnvelope<{ dashboardId: number | null }>(
          GlobalConstants.API_ENDPOINTS.settings.dashboardRuntime.lastDashboardGet,
          {},
          'silent'
        )
      );
      const id = res?.dashboardId;
      return typeof id === 'number' && id > 0 ? id : null;
    } catch {
      return null;
    }
  }

  /** Fire-and-forget save of the user's currently selected dashboard. */
  async setLastDashboardId(id: number | null): Promise<void> {
    try {
      await firstValueFrom(
        this.api.postEnvelope<void>(
          GlobalConstants.API_ENDPOINTS.settings.dashboardRuntime.lastDashboardSet,
          { id },
          'silent'
        )
      );
    } catch {
      // Persistence is best-effort; never block the UI on a failed preference write.
    }
  }

  async widgetData(widgetId: number): Promise<Record<string, unknown>[]> {
    const x = await firstValueFrom(
      this.api.postEnvelope<Record<string, unknown>[]>(
        GlobalConstants.API_ENDPOINTS.settings.dashboardRuntime.widgetData,
        { widgetId },
        'silent'
      )
    );
    return x ?? [];
  }

  /** Paged list with optional name substring filter (server-side). */
  async listQueryDefsPage(params: {
    pageNumber: number;
    pageSize: number;
    nameSearch?: string;
  }): Promise<PageResponse<QueryDefDto>> {
    const body: Record<string, unknown> = {
      pageNumber: params.pageNumber,
      pageSize: params.pageSize,
    };
    const t = params.nameSearch?.trim();
    if (t) {
      body['nameSearch'] = t;
    }
    const raw = await firstValueFrom(
      this.api.postEnvelope<PageResponse<QueryDefDto> | QueryDefDto[]>(
        GlobalConstants.API_ENDPOINTS.settings.queryDef.gets,
        body,
        'silent'
      )
    );
    return SettingsApiService.normalizeQueryDefPage(raw, params.pageNumber, params.pageSize);
  }

  /**
   * Backend returns {@link PageResponse} for `/gets`; older gateways may still expose a bare array in `data`.
   */
  private static normalizeQueryDefPage(
    raw: PageResponse<QueryDefDto> | QueryDefDto[] | null | undefined,
    pageNumber: number,
    pageSize: number,
  ): PageResponse<QueryDefDto> {
    if (raw == null) {
      return {
        items: [],
        totalCount: 0,
        pageNumber,
        pageSize,
        totalPages: 0,
      };
    }
    if (Array.isArray(raw)) {
      const items = raw;
      const totalCount = items.length;
      return {
        items,
        totalCount,
        pageNumber: 0,
        pageSize: Math.max(pageSize, items.length || 1),
        totalPages: totalCount > 0 ? 1 : 0,
      };
    }
    const items = raw.items ?? [];
    const totalCount = raw.totalCount ?? items.length;
    const totalPages =
      raw.totalPages ??
      (pageSize > 0 && totalCount > 0 ? Math.ceil(totalCount / pageSize) : 0);
    return {
      items,
      totalCount,
      pageNumber: raw.pageNumber ?? pageNumber,
      pageSize: raw.pageSize ?? pageSize,
      totalPages,
    };
  }

  /** Full catalog for dropdowns (loads all pages). */
  async listAllQueryDefs(): Promise<QueryDefDto[]> {
    const acc: QueryDefDto[] = [];
    let page = 0;
    const pageSize = 200;
    for (let guard = 0; guard < 500; guard++) {
      const res = await this.listQueryDefsPage({ pageNumber: page, pageSize });
      const items = res.items ?? [];
      acc.push(...items);
      const totalPages = res.totalPages ?? 0;
      if (items.length < pageSize) {
        break;
      }
      if (totalPages > 0 && page + 1 >= totalPages) {
        break;
      }
      if (totalPages === 0) {
        break;
      }
      page++;
    }
    return acc;
  }

  validateSql(sqlText: string): Promise<void> {
    return firstValueFrom(
      this.api.postEnvelope<void>(
        GlobalConstants.API_ENDPOINTS.settings.queryDef.validate,
        { sqlText },
        'errors'
      )
    );
  }

  saveQuery(body: Partial<QueryDefDto> & { name: string; sqlText: string }): Promise<QueryDefDto> {
    return firstValueFrom(
      this.api.postEnvelope<QueryDefDto>(
        GlobalConstants.API_ENDPOINTS.settings.queryDef.save,
        body,
        'success-and-errors'
      )
    );
  }

  deleteQuery(id: number): Promise<void> {
    return firstValueFrom(
      this.api.postEnvelope<void>(
        GlobalConstants.API_ENDPOINTS.settings.queryDef.delete,
        { id },
        'success-and-errors'
      )
    );
  }

  executeQueryTest(sqlText: string): Promise<Record<string, unknown>[]> {
    return firstValueFrom(
      this.api.postEnvelope<Record<string, unknown>[]>(
        GlobalConstants.API_ENDPOINTS.settings.queryDef.executeTest,
        { sqlText },
        'silent'
      )
    ).then((x) => x ?? []);
  }

  async listDashboardDefinitions(): Promise<DashboardSummaryDto[]> {
    const x = await firstValueFrom(
      this.api.postEnvelope<DashboardSummaryDto[]>(
        GlobalConstants.API_ENDPOINTS.settings.dashboardAdmin.definitions,
        {},
        'silent'
      )
    );
    return x ?? [];
  }

  saveDashboard(body: DashboardSaveDto): Promise<DashboardDetailDto> {
    return firstValueFrom(
      this.api.postEnvelope<DashboardDetailDto>(
        GlobalConstants.API_ENDPOINTS.settings.dashboardAdmin.save,
        body,
        'success-and-errors'
      )
    );
  }

  deleteDashboard(id: number): Promise<void> {
    return firstValueFrom(
      this.api.postEnvelope<void>(
        GlobalConstants.API_ENDPOINTS.settings.dashboardAdmin.delete,
        { id },
        'success-and-errors'
      )
    );
  }
}
