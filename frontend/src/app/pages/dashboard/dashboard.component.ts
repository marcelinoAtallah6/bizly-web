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

    void this.dashCtx.loadSummaries().then(() => {
      const list = this.dashCtx.summariesSnapshot;
      if (list.length && this.dashCtx.selectedIdSnapshot == null) {
        void this.dashCtx.selectDashboardById(list[0].id, false);
      }
      this.rebuildDashboardToolbar();
    });
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
