import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Router } from '@angular/router';
import { DashboardDetailDto, DashboardSummaryDto } from '../core/models/settings.models';
import { SettingsApiService } from './settings-api.service';

const LS_ORDER = 'bizly_dashboard_order';

/**
 * Loads dashboards for the current user, keeps optional ordering in localStorage,
 * and holds the selected dashboard detail (loaded by id — no slug in the URL).
 */
@Injectable({ providedIn: 'root' })
export class DashboardContextService {
  private readonly summariesSubject = new BehaviorSubject<DashboardSummaryDto[]>([]);
  private readonly selectedIdSubject = new BehaviorSubject<number | null>(null);
  private readonly detailSubject = new BehaviorSubject<DashboardDetailDto | null>(null);
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);

  readonly summaries$ = this.summariesSubject.asObservable();
  readonly selectedId$ = this.selectedIdSubject.asObservable();
  readonly detail$ = this.detailSubject.asObservable();
  readonly loading$ = this.loadingSubject.asObservable();

  constructor(
    private readonly settingsApi: SettingsApiService,
    private readonly router: Router
  ) {}

  get summariesSnapshot(): DashboardSummaryDto[] {
    return this.summariesSubject.value;
  }

  get selectedIdSnapshot(): number | null {
    return this.selectedIdSubject.value;
  }

  get detailSnapshot(): DashboardDetailDto | null {
    return this.detailSubject.value;
  }

  async loadSummaries(): Promise<void> {
    const list = await this.settingsApi.dashboardSummariesForUser();
    const ordered = this.applySavedOrder(list);
    this.summariesSubject.next(ordered);
    const sid = this.selectedIdSubject.value;
    if (sid != null && !ordered.some((d) => d.id === sid)) {
      this.selectedIdSubject.next(null);
      this.detailSubject.next(null);
    }
  }

  /**
   * Persist drag-and-drop order (dashboard ids only).
   */
  setDashboardOrder(orderedIds: number[]): void {
    localStorage.setItem(LS_ORDER, JSON.stringify(orderedIds));
    const list = this.summariesSubject.value;
    const map = new Map(list.map((d) => [d.id, d]));
    const next: DashboardSummaryDto[] = [];
    for (const id of orderedIds) {
      const x = map.get(id);
      if (x) {
        next.push(x);
      }
    }
    for (const d of list) {
      if (!orderedIds.includes(d.id)) {
        next.push(d);
      }
    }
    this.summariesSubject.next(next);
  }

  async selectDashboardById(id: number | null, navigateHome = true): Promise<void> {
    if (id == null) {
      this.selectedIdSubject.next(null);
      this.detailSubject.next(null);
      return;
    }
    this.selectedIdSubject.next(id);
    this.loadingSubject.next(true);
    try {
      const detail = await this.settingsApi.loadDashboard({ id });
      this.detailSubject.next(detail);
      if (navigateHome) {
        const path = this.router.url.split('?')[0].split('#')[0];
        const segments = path.split('/').filter(Boolean);
        if (!segments.includes('dashboard')) {
          await this.router.navigate(['/dashboard']);
        }
      }
    } finally {
      this.loadingSubject.next(false);
    }
  }

  clearSelection(): void {
    this.selectedIdSubject.next(null);
    this.detailSubject.next(null);
  }

  private applySavedOrder(list: DashboardSummaryDto[]): DashboardSummaryDto[] {
    try {
      const raw = localStorage.getItem(LS_ORDER);
      if (!raw) {
        return list;
      }
      const order = JSON.parse(raw) as number[];
      if (!Array.isArray(order)) {
        return list;
      }
      const map = new Map(list.map((d) => [d.id, d]));
      const out: DashboardSummaryDto[] = [];
      for (const id of order) {
        const x = map.get(id);
        if (x) {
          out.push(x);
          map.delete(id);
        }
      }
      for (const d of map.values()) {
        out.push(d);
      }
      return out;
    } catch {
      return list;
    }
  }
}
