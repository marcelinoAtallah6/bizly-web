import { Component, OnDestroy, OnInit, ViewEncapsulation } from '@angular/core';
import { Subscription } from 'rxjs';
import { DashboardSummaryDto } from 'src/app/core/models/settings.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { DashboardContextService } from 'src/app/services/dashboard-context.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class AppDashboardComponent implements OnInit, OnDestroy {
  dashboards: DashboardSummaryDto[] = [];
  dashboardSelectedId: number | null = null;

  /** Same pattern as list/detail screens: `custom-toolbar` with submenu (`mat-menu`). */
  dashboardToolbar: ToolbarButton[] = [];

  private subs = new Subscription();

  constructor(readonly dashCtx: DashboardContextService) {}

  get pageTitle(): string {
    const name = this.dashCtx.detailSnapshot?.name?.trim();
    return name || 'Dashboard';
  }

  ngOnInit(): void {
    this.subs.add(
      this.dashCtx.summaries$.subscribe((list) => {
        this.dashboards = list;
        this.rebuildDashboardToolbar();
      })
    );
    this.subs.add(
      this.dashCtx.selectedId$.subscribe((id) => {
        this.dashboardSelectedId = id;
        this.rebuildDashboardToolbar();
      })
    );

    void this.bootstrapDashboardSelection();
  }

  /**
   * Loads the navbar dashboards and decides which one to open. Preference order is:
   *   1. The user's currently-selected dashboard (if any — survives in-app navigation).
   *   2. The dashboard the user opened last (persisted server-side via SETTINGS_USER_PREF).
   *   3. The first dashboard in the navbar list (fallback when the user has never picked one).
   */
  private async bootstrapDashboardSelection(): Promise<void> {
    const [, savedId] = await Promise.all([
      this.dashCtx.loadSummaries(),
      this.dashCtx.loadSavedSelection(),
    ]);

    if (this.dashCtx.selectedIdSnapshot != null) {
      this.rebuildDashboardToolbar();
      return;
    }

    const list = this.dashCtx.summariesSnapshot;
    if (!list.length) {
      this.rebuildDashboardToolbar();
      return;
    }

    const preferred = savedId != null && list.some((d) => d.id === savedId) ? savedId : list[0].id;
    await this.dashCtx.selectDashboardById(preferred, false);
    this.rebuildDashboardToolbar();
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  onDashboardSelect(id: number | null): void {
    if (id == null) {
      return;
    }
    void this.dashCtx.selectDashboardById(id, false);
  }

  private rebuildDashboardToolbar(): void {
    if (!this.dashboards.length) {
      this.dashboardToolbar = [];
      return;
    }
    const selectedId = this.dashboardSelectedId;
    this.dashboardToolbar = [
      {
        id: 'dashboard-switcher',
        icon: 'menu',
        tooltip: 'Dashboards',
        hasSubmenu: true,
        submenuItems: this.dashboards.map((d) => ({
          id: `dash-${d.id}`,
          label: d.name,
          icon: selectedId === d.id ? 'check' : 'dashboard_customize',
          action: () => this.onDashboardSelect(d.id),
        })),
      },
    ];
  }
}
