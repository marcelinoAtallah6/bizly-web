import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { firstValueFrom, Subject, Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { DynamicDashboardViewComponent } from 'src/app/pages/dashboard/dynamic-dashboard-view/dynamic-dashboard-view.component';
import {
  DashboardDetailDto,
  DashboardSaveDto,
  DashboardSummaryDto,
  QueryDefDto,
  WidgetSaveDto,
} from 'src/app/core/models/settings.models';
import { GetRoleResponse, GetUserResponse } from 'src/app/core/models/um.models';
import { UmRoleService } from 'src/app/pages/um/services/um-role.service';
import { UmUserService } from 'src/app/pages/um/services/um-user.service';
import { SettingsApiService } from 'src/app/services/settings-api.service';

/** Palette tile — drag onto the widget canvas to add a widget of this type. */
export interface DashboardPaletteItem {
  widgetType: string;
  label: string;
  hint: string;
  icon: string;
}

@Component({
  selector: 'app-dashboardbuilder',
  templateUrl: './dashboardbuilder.component.html',
  styleUrl: './dashboardbuilder.component.scss',
})
export class DashboardbuilderComponent implements OnInit, OnDestroy, AfterViewInit {
  readonly widgetTypes = [
    'KPI',
    'TABLE',
    'LINE_CHART',
    'AREA_CHART',
    'BAR_CHART',
    'STACKED_BAR_CHART',
    'HORIZONTAL_BAR_CHART',
    'PIE_CHART',
    'DONUT_CHART',
    'POLAR_AREA_CHART',
    'RADIAL_BAR_CHART',
    'RADAR_CHART',
    'SCATTER_CHART',
    'BUBBLE_CHART',
    'HEATMAP',
    'RANGE_BAR_CHART',
    'CANDLESTICK_CHART',
    'TREEMAP_CHART',
    'QUICK_ACTION',
    'CALENDAR',
    'CARD_LIST',
  ];

  /** Visual palette (constant — reset after each drag-from-palette so tiles never disappear). */
  readonly paletteSeed: DashboardPaletteItem[] = [
    { widgetType: 'KPI', label: 'KPI', hint: 'Single metric highlight', icon: 'speed' },
    { widgetType: 'TABLE', label: 'Table', hint: 'Rows & columns', icon: 'table_chart' },
    { widgetType: 'LINE_CHART', label: 'Line', hint: 'Trends over time (Apex line)', icon: 'show_chart' },
    { widgetType: 'AREA_CHART', label: 'Area', hint: 'Filled series (Apex area)', icon: 'area_chart' },
    { widgetType: 'BAR_CHART', label: 'Column', hint: 'Vertical bars (Apex bar)', icon: 'bar_chart' },
    {
      widgetType: 'STACKED_BAR_CHART',
      label: 'Stacked columns',
      hint: 'Stacked vertical bars',
      icon: 'stacked_bar_chart',
    },
    {
      widgetType: 'HORIZONTAL_BAR_CHART',
      label: 'Horizontal bar',
      hint: 'Bars left-to-right',
      icon: 'horizontal_split',
    },
    { widgetType: 'PIE_CHART', label: 'Pie', hint: 'Share of total', icon: 'pie_chart' },
    { widgetType: 'DONUT_CHART', label: 'Donut', hint: 'Pie with hollow center', icon: 'donut_large' },
    {
      widgetType: 'POLAR_AREA_CHART',
      label: 'Polar area',
      hint: 'Polar slices (Apex polarArea)',
      icon: 'brightness_low',
    },
    {
      widgetType: 'RADIAL_BAR_CHART',
      label: 'Radial bar',
      hint: 'Circular progress rings',
      icon: 'track_changes',
    },
    { widgetType: 'RADAR_CHART', label: 'Radar', hint: 'Spider / multi-axis', icon: 'polyline' },
    {
      widgetType: 'SCATTER_CHART',
      label: 'Scatter',
      hint: 'Two numeric columns → X,Y',
      icon: 'scatter_plot',
    },
    {
      widgetType: 'BUBBLE_CHART',
      label: 'Bubble',
      hint: 'Three numeric columns → X,Y,size',
      icon: 'bubble_chart',
    },
    { widgetType: 'HEATMAP', label: 'Heatmap', hint: 'Row × column × value', icon: 'grid_on' },
    {
      widgetType: 'RANGE_BAR_CHART',
      label: 'Range bar',
      hint: 'Category + low + high columns',
      icon: 'linear_scale',
    },
    {
      widgetType: 'CANDLESTICK_CHART',
      label: 'Candlestick',
      hint: 'OHLC numeric columns',
      icon: 'candlestick_chart',
    },
    {
      widgetType: 'TREEMAP_CHART',
      label: 'Treemap',
      hint: 'Label + value hierarchy',
      icon: 'account_tree',
    },
    { widgetType: 'QUICK_ACTION', label: 'Quick actions', hint: 'Shortcut buttons', icon: 'bolt' },
    {
      widgetType: 'CALENDAR',
      label: 'Calendar',
      hint: 'Monthly events with filters & day panel',
      icon: 'calendar_month',
    },
    {
      widgetType: 'CARD_LIST',
      label: 'Card list',
      hint: 'Stacked cards (follow-ups, tasks)',
      icon: 'view_agenda',
    },
  ];

  paletteItems: DashboardPaletteItem[] = [];

  loading = false;
  saving = false;
  definitions: DashboardSummaryDto[] = [];
  queries: QueryDefDto[] = [];
  /** True while refetching saved queries for the edit panel dropdown. */
  queriesLoading = false;

  selectedKey: string | number = 'new';

  name = '';
  slug = '';
  description = '';
  builtin = false;

  /** Grant role names (matches SETTINGS_DASH_ROLE_GRANT). */
  selectedGrantRoles: string[] = [];
  allRoles: GetRoleResponse[] = [];

  /** Grant usernames (SETTINGS_DASH_USER_GRANT). */
  selectedUsernames: string[] = [];
  userCatalog: GetUserResponse[] = [];
  filteredUsersForPick: GetUserResponse[] = [];
  userSearchTerm = '';
  loadingUsers = false;
  readonly userPageSize = 100;
  maxUserPagesToPrefetch = 25;

  dashboardId: number | null = null;
  widgets: WidgetSaveDto[] = [];

  /** Which widget row is edited in the middle panel (null = none). */
  selectedWidgetIndex: number | null = 0;

  /** Stable object for live preview — avoids re-creating DTO every CD (fixes freeze / chart reload loop). */
  previewSnapshot: DashboardDetailDto | null = null;

  /** While true, preview skips remote query reload (drag in progress). */
  previewDragSuspend = false;

  private readonly previewPatch$ = new Subject<void>();
  private previewPatchSub?: Subscription;

  /** Top overview bar (same `custom-toolbar` pattern as other screens). */
  overviewToolbar: ToolbarButton[] = [];

  /** Right slide-in panel for widget fields. */
  editPanelOpen = false;

  /** Direct grid interactions (mutually exclusive helpers). */
  resizeMode = false;
  reorderMode = false;

  @ViewChild(DynamicDashboardViewComponent) private previewView?: DynamicDashboardViewComponent;

  constructor(
    private readonly settingsApi: SettingsApiService,
    private readonly umRoleService: UmRoleService,
    private readonly umUserService: UmUserService
  ) {}

  ngOnInit(): void {
    this.resetPalette();
    this.previewPatchSub = this.previewPatch$.pipe(debounceTime(450)).subscribe(() => {
      this.refreshPreviewSnapshot();
    });
    void this.bootstrap();
  }

  ngAfterViewInit(): void {
    this.syncOverviewToolbar();
  }

  ngOnDestroy(): void {
    this.previewPatchSub?.unsubscribe();
  }

  /** Debounced refresh when editing titles/sliders so preview stays smooth. */
  schedulePreviewRefresh(): void {
    this.previewPatch$.next();
  }

  setPreviewDragSuspend(active: boolean): void {
    this.previewDragSuspend = active;
  }

  /** Rebuild preview DTO from current editor model (call after structural changes immediately). */
  refreshPreviewSnapshot(): void {
    this.previewSnapshot = {
      id: this.dashboardId ?? 0,
      name: this.name.trim() || 'Preview',
      slug: (this.slug.trim() || 'preview').toLowerCase().replace(/\s+/g, '-'),
      description: this.description.trim() || undefined,
      builtin: this.builtin,
      widgets: this.widgets.map((w, i) => ({
        id: w.id ?? -(i + 1),
        widgetType: w.widgetType,
        title: w.title,
        queryDefId: w.queryDefId ?? null,
        queryName: null,
        configJson: w.configJson ?? null,
        gridX: w.gridX,
        gridY: w.gridY,
        gridW: w.gridW,
        gridH: w.gridH,
        refreshSec: w.refreshSec ?? null,
        sortOrder: i,
      })),
    };
  }

  /** Map preview tile click → editor selection. */
  selectWidgetByPreviewId(widgetId: number): void {
    const i = this.widgets.findIndex((x, idx) => {
      const pid = x.id ?? -(idx + 1);
      return pid === widgetId;
    });
    if (i >= 0) {
      this.selectedWidgetIndex = i;
      this.syncOverviewToolbar();
    }
  }

  get overviewBarTitle(): string {
    if (this.selectedWidgetIndex == null) {
      return '';
    }
    const w = this.widgets[this.selectedWidgetIndex];
    const t = w?.title?.trim();
    return t ? `Overview · ${t}` : 'Overview';
  }

  get selectedPreviewWidgetId(): number | null {
    if (this.selectedWidgetIndex == null) {
      return null;
    }
    const w = this.widgets[this.selectedWidgetIndex];
    if (!w) {
      return null;
    }
    return w.id ?? -(this.selectedWidgetIndex + 1);
  }

  private syncOverviewToolbar(): void {
    if (this.selectedWidgetIndex == null) {
      this.overviewToolbar = [];
      return;
    }
    const saving = this.saving;
    const canSave = !!this.name.trim() && !!this.slug.trim();
    this.overviewToolbar = [
      {
        id: 'edit',
        icon: 'edit',
        tooltip: 'Edit widget',
        action: () => {
          this.editPanelOpen = true;
        },
      },
      {
        id: 'resize',
        icon: 'crop_free',
        tooltip: this.resizeMode ? 'Exit resize mode' : 'Resize mode',
        ...(this.resizeMode ? { color: 'primary' as const } : {}),
        action: () => this.toggleResizeMode(),
      },
      {
        id: 'reorder',
        icon: 'swap_vert',
        tooltip: this.reorderMode ? 'Exit reorder mode' : 'Reorder mode',
        ...(this.reorderMode ? { color: 'primary' as const } : {}),
        action: () => this.toggleReorderMode(),
      },
      {
        id: 'dup',
        icon: 'content_copy',
        tooltip: 'Duplicate widget',
        action: () => this.duplicateSelectedWidget(),
      },
      {
        id: 'del',
        icon: 'delete_outline',
        tooltip: 'Delete widget',
        color: 'warn',
        action: () => this.removeSelectedWidget(),
      },
      {
        id: 'save',
        icon: 'save',
        tooltip: 'Save dashboard',
        disabled: saving || !canSave,
        action: () => void this.save(),
      },
      {
        id: 'refresh',
        icon: 'refresh',
        tooltip: 'Refresh all widgets',
        disabled: saving,
        action: () => this.refreshAllPreview(),
      },
    ];
  }

  toggleResizeMode(): void {
    this.resizeMode = !this.resizeMode;
    if (this.resizeMode) {
      this.reorderMode = false;
    }
    this.syncOverviewToolbar();
  }

  toggleReorderMode(): void {
    this.reorderMode = !this.reorderMode;
    if (this.reorderMode) {
      this.resizeMode = false;
    }
    this.syncOverviewToolbar();
  }

  refreshAllPreview(): void {
    this.previewView?.refreshPreviewData();
  }

  onPreviewResizeDelta(e: { widgetId: number; gridW: number; gridH: number }): void {
    const i = this.widgets.findIndex((x, idx) => (x.id ?? -(idx + 1)) === e.widgetId);
    if (i < 0) {
      return;
    }
    const w = this.widgets[i];
    w.gridW = e.gridW;
    w.gridH = e.gridH;
    this.clampGrid(w);
  }

  /** Push widget edits to preview immediately + queue chart/query reload (fixes type/query/grid lag). */
  syncEditorToPreview(): void {
    this.refreshPreviewSnapshot();
    this.schedulePreviewRefresh();
  }

  compareDashboardKey(a: string | number, b: string | number): boolean {
    return a === b;
  }

  /** mat-select multiple compares selected strings to mat-option values */
  compareGrantRoleVal(a: string, b: string): boolean {
    return (a ?? '').trim().toLowerCase() === (b ?? '').trim().toLowerCase();
  }

  compareQueryDefId(o1: number | null | undefined, o2: number | null | undefined): boolean {
    return (o1 ?? null) === (o2 ?? null);
  }

  /** Reload saved queries when the edit drawer opens (fixes empty dropdown after cold load / API timing). */
  async onEditPanelOpened(open: boolean): Promise<void> {
    if (!open) {
      return;
    }
    await this.refreshQueryCatalog();
  }

  private async refreshQueryCatalog(): Promise<void> {
    this.queriesLoading = true;
    try {
      this.queries = await this.settingsApi.listAllQueryDefs();
    } catch {
      /* keep existing */
    } finally {
      this.queriesLoading = false;
    }
  }

  onPreviewFloatingAction(ev: {
    action: 'edit' | 'duplicate' | 'delete' | 'toggleResize' | 'toggleReorder';
    widgetId: number;
  }): void {
    this.selectWidgetByPreviewId(ev.widgetId);
    switch (ev.action) {
      case 'edit':
        this.editPanelOpen = true;
        break;
      case 'duplicate':
        this.duplicateSelectedWidget();
        break;
      case 'delete':
        this.removeSelectedWidget();
        break;
      case 'toggleResize':
        this.toggleResizeMode();
        break;
      case 'toggleReorder':
        this.toggleReorderMode();
        break;
    }
  }

  /** Palette is a drag source only — nothing may be dropped onto it. */
  paletteNoIncoming = () => false;

  private resetPalette(): void {
    this.paletteItems = this.paletteSeed.map((p) => ({ ...p }));
  }

  async bootstrap(): Promise<void> {
    this.loading = true;
    try {
      try {
        await Promise.all([this.loadAllRoles(), this.prefetchUsers()]);
      } catch {
        /* UM lists optional — builder still usable */
      }
      let defs: DashboardSummaryDto[] = [];
      let qs: QueryDefDto[] = [];
      try {
        defs = await this.settingsApi.listDashboardDefinitions();
      } catch {
        defs = [];
      }
      try {
        qs = await this.settingsApi.listAllQueryDefs();
      } catch {
        qs = [];
      }
      this.definitions = defs ?? [];
      this.queries = qs ?? [];
    } finally {
      this.resetForm();
      this.loading = false;
    }
  }

  private async loadAllRoles(): Promise<void> {
    const acc: GetRoleResponse[] = [];
    let page = 0;
    const pageSize = 200;
    while (true) {
      const res = await firstValueFrom(this.umRoleService.gets({ pageNumber: page, pageSize }));
      const items = res.items ?? [];
      acc.push(...items);
      if (items.length < pageSize) {
        break;
      }
      page++;
    }
    this.allRoles = acc.sort((a, b) => a.name.localeCompare(b.name));
  }

  /** Prefetch users in pages for client-side search (UM gets API is paginated). */
  private async prefetchUsers(): Promise<void> {
    this.loadingUsers = true;
    try {
      const acc: GetUserResponse[] = [];
      for (let page = 0; page < this.maxUserPagesToPrefetch; page++) {
        const res = await firstValueFrom(
          this.umUserService.gets({ pageNumber: page, pageSize: this.userPageSize })
        );
        const items = res.items ?? [];
        acc.push(...items);
        if (items.length < this.userPageSize) {
          break;
        }
      }
      this.userCatalog = acc;
      this.applyUserFilter();
    } finally {
      this.loadingUsers = false;
    }
  }

  onUserSearchChange(term: string): void {
    this.userSearchTerm = term;
    this.applyUserFilter();
  }

  private applyUserFilter(): void {
    const t = this.userSearchTerm.trim().toLowerCase();
    const pool = this.userCatalog.filter((u) => !this.selectedUsernames.includes(u.username));
    if (!t) {
      this.filteredUsersForPick = pool.slice(0, 60);
      return;
    }
    this.filteredUsersForPick = pool
      .filter((u) => {
        const hay = `${u.username} ${u.email} ${u.firstName} ${u.lastName}`.toLowerCase();
        return hay.includes(t);
      })
      .slice(0, 80);
  }

  onUserPicked(event: MatAutocompleteSelectedEvent): void {
    const raw = event.option?.value;
    const username = typeof raw === 'string' ? raw.trim() : (raw as GetUserResponse | undefined)?.username?.trim();
    if (username && !this.selectedUsernames.includes(username)) {
      this.selectedUsernames = [...this.selectedUsernames, username];
    }
    this.userSearchTerm = '';
    this.applyUserFilter();
  }

  /** Show label in the input after picking a user (avoids "[object Object]"). */
  displayPickedUser = (value: string | null): string => {
    if (value == null || value === '') {
      return '';
    }
    const u = this.userCatalog.find((x) => x.username === value);
    return u ? `${u.username} — ${u.email ?? ''}` : value;
  };

  /** Strip Spring-style ROLE_ prefix so mat-select matches UM role names. */
  private normalizeRoleNameForSelect(raw: string | null | undefined): string {
    if (raw == null) {
      return '';
    }
    let t = String(raw).trim();
    if (t.length > 5 && t.toUpperCase().startsWith('ROLE_')) {
      t = t.substring(5);
    }
    return t;
  }

  removeUsername(name: string): void {
    this.selectedUsernames = this.selectedUsernames.filter((x) => x !== name);
    this.applyUserFilter();
  }

  resetForm(): void {
    this.selectedKey = 'new';
    this.dashboardId = null;
    this.name = '';
    this.slug = '';
    this.description = '';
    this.builtin = false;
    this.selectedGrantRoles = [];
    this.selectedUsernames = [];
    this.userSearchTerm = '';
    this.applyUserFilter();
    this.widgets = [this.defaultWidget(0)];
    this.selectedWidgetIndex = 0;
    this.resizeMode = false;
    this.reorderMode = false;
    this.editPanelOpen = false;
    this.refreshPreviewSnapshot();
    this.syncOverviewToolbar();
  }

  defaultWidget(index: number, widgetType = 'TABLE'): WidgetSaveDto {
    return {
      widgetType,
      title: this.defaultTitleForType(widgetType, index),
      queryDefId: this.queries[0]?.id ?? undefined,
      configJson: undefined,
      gridX: 0,
      gridY: index * 2,
      gridW: 6,
      gridH: 2,
      refreshSec: undefined,
      sortOrder: index,
    };
  }

  defaultTitleForType(widgetType: string, index: number): string {
    const t = (widgetType || '').toUpperCase();
    const map: Record<string, string> = {
      KPI: 'KPI',
      TABLE: 'Data table',
      LINE_CHART: 'Line chart',
      AREA_CHART: 'Area chart',
      BAR_CHART: 'Column chart',
      STACKED_BAR_CHART: 'Stacked columns',
      HORIZONTAL_BAR_CHART: 'Horizontal bar',
      PIE_CHART: 'Pie chart',
      DONUT_CHART: 'Donut chart',
      POLAR_AREA_CHART: 'Polar area',
      RADIAL_BAR_CHART: 'Radial bar',
      RADAR_CHART: 'Radar chart',
      SCATTER_CHART: 'Scatter chart',
      BUBBLE_CHART: 'Bubble chart',
      HEATMAP: 'Heatmap',
      RANGE_BAR_CHART: 'Range bar',
      CANDLESTICK_CHART: 'Candlestick',
      TREEMAP_CHART: 'Treemap',
      QUICK_ACTION: 'Quick actions',
      CALENDAR: 'Calendar',
      CARD_LIST: 'Card list',
    };
    return map[t] || `Widget ${index + 1}`;
  }

  humanizeType(widgetType: string): string {
    return (widgetType || '').replace(/_/g, ' ');
  }

  async onSelectionChange(): Promise<void> {
    if (this.selectedKey === 'new') {
      this.resetForm();
      return;
    }
    const id = Number(this.selectedKey);
    if (!Number.isFinite(id)) {
      return;
    }
    this.loading = true;
    try {
      const d = await this.settingsApi.loadDashboard({ id });
      if (!d) {
        return;
      }
      this.mergeUnknownRoles(d.grantRoles ?? []);
      this.applyDetail(d);
    } finally {
      this.loading = false;
    }
  }

  /** Ensure grant role strings from API appear in the multi-select even if missing from UM catalog. */
  private mergeUnknownRoles(grants: string[]): void {
    const known = new Set(this.allRoles.map((r) => r.name.toUpperCase()));
    for (const g of grants) {
      const gn = this.normalizeRoleNameForSelect(g);
      if (!gn) {
        continue;
      }
      if (!known.has(gn.toUpperCase())) {
        this.allRoles.push({ id: 0, name: gn } as GetRoleResponse);
        known.add(gn.toUpperCase());
      }
    }
    this.allRoles.sort((a, b) => a.name.localeCompare(b.name));
  }

  private applyDetail(d: DashboardDetailDto): void {
    this.dashboardId = d.id;
    this.selectedKey = d.id;
    this.name = d.name;
    this.slug = d.slug;
    this.description = d.description ?? '';
    this.builtin = !!d.builtin;
    this.selectedGrantRoles = (d.grantRoles ?? []).map((g) => this.normalizeRoleNameForSelect(g)).filter(Boolean);
    this.selectedUsernames = [...(d.grantUsernames ?? [])];
    this.widgets =
      d.widgets?.map((w, i) => ({
        id: w.id,
        widgetType: w.widgetType,
        title: w.title,
        queryDefId: w.queryDefId ?? undefined,
        configJson: w.configJson ?? undefined,
        gridX: w.gridX,
        gridY: w.gridY,
        gridW: w.gridW,
        gridH: w.gridH,
        refreshSec: w.refreshSec ?? undefined,
        sortOrder: w.sortOrder ?? i,
      })) ?? [];
    if (!this.widgets.length) {
      this.widgets = [this.defaultWidget(0)];
    }
    this.selectedWidgetIndex = this.widgets.length ? 0 : null;
    this.applyUserFilter();
    this.refreshPreviewSnapshot();
    this.resizeMode = false;
    this.reorderMode = false;
    this.syncOverviewToolbar();
  }

  addWidget(): void {
    this.widgets.push(this.defaultWidget(this.widgets.length));
    this.selectedWidgetIndex = this.widgets.length - 1;
    this.refreshPreviewSnapshot();
    this.syncOverviewToolbar();
  }

  selectWidget(index: number): void {
    this.selectedWidgetIndex = index;
    this.syncOverviewToolbar();
  }

  duplicateSelectedWidget(): void {
    const i = this.selectedWidgetIndex;
    if (i == null) {
      return;
    }
    const src = this.widgets[i];
    const copy: WidgetSaveDto = {
      ...src,
      id: undefined,
      title: `${src.title || 'Widget'} (copy)`,
      sortOrder: i + 1,
    };
    this.widgets.splice(i + 1, 0, copy);
    this.selectedWidgetIndex = i + 1;
    this.refreshPreviewSnapshot();
    this.syncOverviewToolbar();
  }

  removeWidget(i: number): void {
    this.widgets.splice(i, 1);
    if (!this.widgets.length) {
      this.widgets.push(this.defaultWidget(0));
      this.selectedWidgetIndex = 0;
      this.refreshPreviewSnapshot();
      this.syncOverviewToolbar();
      return;
    }
    const sel = this.selectedWidgetIndex;
    if (sel != null) {
      if (sel === i) {
        this.selectedWidgetIndex = Math.min(i, this.widgets.length - 1);
      } else if (sel > i) {
        this.selectedWidgetIndex = sel - 1;
      }
    }
    this.refreshPreviewSnapshot();
    this.syncOverviewToolbar();
  }

  removeSelectedWidget(): void {
    if (this.selectedWidgetIndex != null) {
      this.removeWidget(this.selectedWidgetIndex);
    }
  }

  /**
   * Reorder within canvas, or insert a new widget when dragging from the type palette.
   */
  onWidgetDrop(event: CdkDragDrop<any>): void {
    if (event.previousContainer.id === 'palette-list') {
      const prevData = event.previousContainer.data as DashboardPaletteItem[];
      const src =
        (event.item.data as DashboardPaletteItem) ??
        prevData[event.previousIndex];
      if (!src?.widgetType) {
        this.resetPalette();
        return;
      }
      const insertAt = event.currentIndex;
      const w = this.defaultWidget(insertAt, src.widgetType);
      this.widgets.splice(insertAt, 0, w);
      this.selectedWidgetIndex = insertAt;
      this.resetPalette();
      this.reflowWidgetsGridPacking();
      this.syncOverviewToolbar();
      return;
    }

    if (event.previousContainer === event.container) {
      const from = event.previousIndex;
      const to = event.currentIndex;
      const sel = this.selectedWidgetIndex;
      moveItemInArray(this.widgets, from, to);
      if (sel != null) {
        if (sel === from) {
          this.selectedWidgetIndex = to;
        } else if (from < to) {
          if (sel > from && sel <= to) {
            this.selectedWidgetIndex = sel - 1;
          }
        } else if (from > to) {
          if (sel >= to && sel < from) {
            this.selectedWidgetIndex = sel + 1;
          }
        }
      }
      this.reflowWidgetsGridPacking();
    }
    this.syncOverviewToolbar();
  }

  /**
   * Recompute gridX/gridY from list order: flow left-to-right in the 12-col grid, wrap to the next row.
   * Fixes “always stacks under” after drag-reorder because stale coordinates ignored new order.
   */
  private reflowWidgetsGridPacking(): void {
    const maxCols = 12;
    let rowStart = 0;
    let col = 0;
    let rowMaxH = 0;

    for (const w of this.widgets) {
      const gw = Math.min(Math.max(1, Math.round(Number(w.gridW) || 6)), maxCols);
      const gh = Math.min(Math.max(1, Math.round(Number(w.gridH) || 2)), 12);

      if (col > 0 && col + gw > maxCols) {
        rowStart += rowMaxH;
        col = 0;
        rowMaxH = 0;
      }

      w.gridX = col;
      w.gridY = rowStart;
      col += gw;
      rowMaxH = Math.max(rowMaxH, gh);
    }

    for (const w of this.widgets) {
      this.applyGridClamps(w);
    }
    this.refreshPreviewSnapshot();
  }

  private applyGridClamps(w: WidgetSaveDto): void {
    const clamp = (n: unknown, lo: number, hi: number, fallback: number): number => {
      const x = Number(n);
      if (!Number.isFinite(x)) {
        return fallback;
      }
      return Math.min(hi, Math.max(lo, Math.round(x)));
    };
    w.gridW = clamp(w.gridW, 1, 12, 6);
    w.gridH = clamp(w.gridH, 1, 12, 2);
    w.gridX = clamp(w.gridX, 0, 11, 0);
    w.gridY = clamp(w.gridY, 0, 48, 0);
  }

  /** Keep grid sizes within sensible bounds after slider or manual edits. */
  clampGrid(w: WidgetSaveDto): void {
    this.applyGridClamps(w);
    this.refreshPreviewSnapshot();
  }

  previewIcon(widgetType: string): string {
    const t = (widgetType || '').toUpperCase();
    const map: Record<string, string> = {
      KPI: 'speed',
      TABLE: 'table_chart',
      HEATMAP: 'grid_on',
      LINE_CHART: 'show_chart',
      AREA_CHART: 'area_chart',
      BAR_CHART: 'bar_chart',
      STACKED_BAR_CHART: 'stacked_bar_chart',
      HORIZONTAL_BAR_CHART: 'horizontal_split',
      PIE_CHART: 'pie_chart',
      DONUT_CHART: 'donut_large',
      POLAR_AREA_CHART: 'brightness_low',
      RADIAL_BAR_CHART: 'track_changes',
      RADAR_CHART: 'polyline',
      SCATTER_CHART: 'scatter_plot',
      BUBBLE_CHART: 'bubble_chart',
      RANGE_BAR_CHART: 'linear_scale',
      CANDLESTICK_CHART: 'candlestick_chart',
      TREEMAP_CHART: 'account_tree',
      QUICK_ACTION: 'bolt',
      CALENDAR: 'calendar_month',
      CARD_LIST: 'view_agenda',
    };
    return map[t] ?? 'widgets';
  }

  async save(): Promise<void> {
    if (!this.name.trim() || !this.slug.trim()) {
      return;
    }
    const body: DashboardSaveDto = {
      id: this.dashboardId ?? undefined,
      name: this.name.trim(),
      slug: this.slug
        .trim()
        .toLowerCase()
        .replace(/\s+/g, '-'),
      description: this.description.trim() || undefined,
      layoutJson: undefined,
      builtin: this.builtin,
      widgets: this.widgets.map((w, i) => {
        this.clampGrid(w);
        return {
          ...w,
          sortOrder: i,
        };
      }),
      grantRoles: this.selectedGrantRoles.filter(Boolean),
      grantUsernames: this.selectedUsernames.filter(Boolean),
    };

    this.saving = true;
    try {
      const saved = await this.settingsApi.saveDashboard(body);
      this.definitions = await this.settingsApi.listDashboardDefinitions();
      this.mergeUnknownRoles(saved.grantRoles ?? []);
      this.applyDetail(saved);
      this.selectedKey = saved.id;
    } finally {
      this.saving = false;
      this.syncOverviewToolbar();
    }
  }

  async remove(): Promise<void> {
    if (this.dashboardId == null) {
      return;
    }
    if (!confirm('Delete this dashboard and its widgets?')) {
      return;
    }
    await this.settingsApi.deleteDashboard(this.dashboardId);
    this.definitions = await this.settingsApi.listDashboardDefinitions();
    this.resetForm();
  }
}
