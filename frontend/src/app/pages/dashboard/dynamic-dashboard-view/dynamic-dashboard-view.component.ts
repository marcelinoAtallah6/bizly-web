import { CdkDragDrop } from '@angular/cdk/drag-drop';
import {
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { Subject, Subscription, interval } from 'rxjs';
import { debounceTime, filter } from 'rxjs/operators';
import { DashboardDetailDto, QueryDefDto, WidgetDetailDto, WidgetSaveDto } from 'src/app/core/models/settings.models';
import { SettingsApiService } from 'src/app/services/settings-api.service';
import { DashboardContextService } from 'src/app/services/dashboard-context.service';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import {
  DashboardCalendarConfig,
} from '../widgets/dashboard-calendar-widget/dashboard-calendar-widget.component';
import {
  DashboardCardListConfig,
} from '../widgets/dashboard-card-list-widget/dashboard-card-list-widget.component';

/** Parse JDBC/Oracle numbers that may arrive as strings (with commas / NBSP). */
function parseNumericLoose(v: unknown): number {
  if (v === null || v === undefined) {
    return 0;
  }
  if (typeof v === 'number') {
    return Number.isFinite(v) ? v : 0;
  }
  const s = String(v).trim().replace(/\u00a0/g, '').replace(/,/g, '');
  if (s === '') {
    return 0;
  }
  const n = Number(s);
  return Number.isFinite(n) ? n : 0;
}

function isNumericLike(v: unknown): boolean {
  if (v === null || v === undefined) {
    return false;
  }
  if (typeof v === 'number') {
    return Number.isFinite(v);
  }
  const s = String(v).trim();
  if (s === '') {
    return false;
  }
  const n = Number(s.replace(/\u00a0/g, '').replace(/,/g, ''));
  return !Number.isNaN(n);
}

@Component({
  selector: 'app-dynamic-dashboard-view',
  templateUrl: './dynamic-dashboard-view.component.html',
  styleUrl: './dynamic-dashboard-view.component.scss',
})
export class DynamicDashboardViewComponent implements OnInit, OnChanges, OnDestroy {
  /** Builder preview: bypass runtime context and load rows via saved query SQL when widget id is missing. */
  @Input() previewMode = false;
  @Input() previewDetail: DashboardDetailDto | null = null;
  @Input() previewQueryDefs: QueryDefDto[] = [];
  /** Same array reference as builder `widgets` — required for overview `cdkDropList` + reorder. */
  @Input() previewWidgetsBridge: WidgetSaveDto[] | null = null;
  /** Builder: drag handles on live grid (palette drops + reorder). */
  @Input() previewDragReorder = false;
  /** Skip preview data reload while dragging (avoids charts reloading / UI freeze). */
  @Input() suspendPreviewReload = false;
  /** Highlights selection + floating toolbar (builder). */
  @Input() previewSelectedWidgetId: number | null = null;
  /** Shows SE resize handle on selected widget (builder). */
  @Input() previewResizeMode = false;
  /**
   * When true (dashboard builder), `cdkDropList` lives on the parent so palette ↔ overview CDK connection works.
   * Child only renders `dash-grid` + `cdkDrag` tiles.
   */
  @Input() previewDropListInParent = false;

  @Output() previewGridDrop = new EventEmitter<CdkDragDrop<unknown>>();
  @Output() previewWidgetSelect = new EventEmitter<number>();
  @Output() previewDragActive = new EventEmitter<boolean>();
  @Output() previewResizeDelta = new EventEmitter<{ widgetId: number; gridW: number; gridH: number }>();
  @Output() previewFloatingAction = new EventEmitter<{
    action: 'edit' | 'duplicate' | 'delete' | 'toggleResize' | 'toggleReorder';
    widgetId: number;
  }>();

  detail: DashboardDetailDto | null = null;
  loadingDetail = false;
  widgetsLoading = false;
  widgetRows = new Map<number, Record<string, unknown>[]>();
  /** Apex partial options built from SQL rows (typed loosely for template bindings). */
  apexByWidget = new Map<number, unknown>();
  quickActions: { label: string; route: string }[] = [];

  readonly kpiTones = ['primary', 'warning', 'accent', 'error', 'success'] as const;

  private subs = new Subscription();
  private readonly previewReload$ = new Subject<void>();
  /** Avoid preview query spam when parent passes a fresh object each CD (builder getter). */
  private lastPreviewInputSig = '';

  private resizePointerId: number | null = null;
  private resizeSession: {
    widgetId: number;
    startX: number;
    startY: number;
    origW: number;
    origH: number;
    cellW: number;
    rowH: number;
  } | null = null;

  constructor(
    private readonly ctx: DashboardContextService,
    private readonly settingsApi: SettingsApiService,
    private readonly menuPerm: MenuPermissionService
  ) {}

  /**
   * Filters out quick-action shortcuts pointing to routes the current role can't view. Builder
   * preview keeps every shortcut so the author can still see what they're editing — the runtime
   * pass (and only the runtime pass) hides links the JWT matrix forbids.
   */
  private filterAccessibleActions<T extends { route?: string | null }>(actions: T[]): T[] {
    if (this.previewMode) {
      return actions;
    }
    return (actions ?? []).filter((a) => {
      const r = (a?.route ?? '').toString().trim();
      if (!r) {
        return false;
      }
      return this.menuPerm.can(r, 'view');
    });
  }

  /** Runtime dashboard uses `detail`; builder preview uses `previewDetail`. */
  get effectiveDetail(): DashboardDetailDto | null {
    return this.previewMode ? this.previewDetail : this.detail;
  }

  ngOnInit(): void {
    if (!this.previewMode) {
      this.subs.add(
        this.ctx.detail$.subscribe((d) => {
          this.detail = d;
          void this.reloadWidgets();
        })
      );
      this.subs.add(this.ctx.loading$.subscribe((l) => (this.loadingDetail = l)));
      this.subs.add(
        interval(60000)
          .pipe(filter(() => !this.previewMode))
          .subscribe(() => void this.refreshTimedWidgets())
      );
    }

    this.subs.add(
      this.previewReload$
        .pipe(
          debounceTime(450),
          filter(() => !this.suspendPreviewReload)
        )
        .subscribe(() => void this.reloadWidgets())
    );
  }

  ngOnChanges(_changes: SimpleChanges): void {
    if (!this.previewMode || this.suspendPreviewReload) {
      return;
    }
    const sig = JSON.stringify({
      widgets: this.previewDetail?.widgets,
      qids: (this.previewQueryDefs ?? []).map((q) => q.id),
    });
    if (sig === this.lastPreviewInputSig) {
      return;
    }
    this.lastPreviewInputSig = sig;
    this.previewReload$.next();
  }

  /** Grid iteration: builder preview lists every widget (including QUICK_ACTION); runtime hides QA via widgets(). */
  layoutWidgets(): WidgetDetailDto[] {
    if (this.previewMode) {
      return this.effectiveDetail?.widgets ?? [];
    }
    return this.widgets();
  }

  /** Manual refresh of preview queries/charts (builder “Refresh all”). */
  refreshPreviewData(): void {
    if (!this.previewMode) {
      return;
    }
    void this.reloadWidgets();
  }

  trackWidgetById(_i: number, w: WidgetDetailDto): number {
    return w.id;
  }

  /**
   * CSS grid 12 columns — use gridX/gridW so builder “Style” column/row fields actually move tiles.
   * Lines are 1-based; gridX 0 = column 1, etc.
   */
  gridColumnStyle(w: WidgetDetailDto): string {
    const gx = Math.max(0, Math.min(11, Math.floor(Number(w.gridX) || 0)));
    const rawW = Math.max(1, Math.min(12, Math.floor(Number(w.gridW) || 1)));
    const span = Math.min(rawW, 12 - gx);
    return `${gx + 1} / span ${span}`;
  }

  /** Implicit rows: start line from gridY, span gridH. */
  gridRowStyle(w: WidgetDetailDto): string {
    const gy = Math.max(0, Math.floor(Number(w.gridY) || 0));
    const gh = Math.max(1, Math.min(24, Math.floor(Number(w.gridH) || 1)));
    return `${gy + 1} / span ${gh}`;
  }

  emitGridDrop(ev: CdkDragDrop<WidgetSaveDto[]>): void {
    this.previewGridDrop.emit(ev as CdkDragDrop<unknown>);
  }

  onPreviewTileClick(widgetId: number, ev: MouseEvent): void {
    const t = ev.target as HTMLElement | null;
    if (t?.closest?.('.preview-drag-handle')) {
      return;
    }
    if (t?.closest?.('.builder-widget-float-bar')) {
      return;
    }
    if (t?.closest?.('.builder-resize-overlay')) {
      return;
    }
    if ((ev.target as HTMLElement)?.closest?.('a[routerLink]')) {
      return;
    }
    this.previewWidgetSelect.emit(widgetId);
  }

  emitFloatingAction(
    action: 'edit' | 'duplicate' | 'delete' | 'toggleResize' | 'toggleReorder',
    widgetId: number
  ): void {
    this.previewFloatingAction.emit({ action, widgetId });
  }

  onResizePointerDown(w: WidgetDetailDto, ev: PointerEvent): void {
    if (!this.previewMode || !this.previewResizeMode) {
      return;
    }
    ev.preventDefault();
    ev.stopPropagation();
    const el = ev.currentTarget as HTMLElement;
    el.setPointerCapture(ev.pointerId);
    this.resizePointerId = ev.pointerId;
    const wrap = el.closest('.dash-grid-item-wrap') as HTMLElement | null;
    const grid = wrap?.parentElement as HTMLElement | null;
    const gw = grid?.getBoundingClientRect().width ?? 1;
    const cellW = Math.max(1, gw / 12);
    const gh = Math.max(1, w.gridH ?? 2);
    const rowH = wrap ? Math.max(36, wrap.offsetHeight / gh) : 80;
    this.resizeSession = {
      widgetId: w.id,
      startX: ev.clientX,
      startY: ev.clientY,
      origW: w.gridW,
      origH: w.gridH,
      cellW,
      rowH,
    };
  }

  @HostListener('document:pointermove', ['$event'])
  onResizePointerMove(ev: PointerEvent): void {
    if (this.resizePointerId == null || ev.pointerId !== this.resizePointerId || !this.resizeSession) {
      return;
    }
    const s = this.resizeSession;
    const dx = ev.clientX - s.startX;
    const dy = ev.clientY - s.startY;
    const nextW = Math.max(1, Math.min(12, s.origW + Math.round(dx / s.cellW)));
    const nextH = Math.max(1, Math.min(12, s.origH + Math.round(dy / s.rowH)));
    this.previewResizeDelta.emit({ widgetId: s.widgetId, gridW: nextW, gridH: nextH });
  }

  @HostListener('document:pointerup', ['$event'])
  @HostListener('document:pointercancel', ['$event'])
  onResizePointerEnd(ev: PointerEvent): void {
    if (this.resizePointerId == null || ev.pointerId !== this.resizePointerId) {
      return;
    }
    this.resizePointerId = null;
    this.resizeSession = null;
  }

  onOverviewDragStart(): void {
    this.previewDragActive.emit(true);
  }

  onOverviewDragEnd(): void {
    this.previewDragActive.emit(false);
  }

  get previewDropListData(): WidgetSaveDto[] {
    if (this.previewMode && this.previewWidgetsBridge != null) {
      return this.previewWidgetsBridge;
    }
    return [];
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  widgets(): WidgetDetailDto[] {
    return this.effectiveDetail?.widgets?.filter((w) => (w.widgetType || '').toUpperCase() !== 'QUICK_ACTION') ?? [];
  }

  /** Re-fetch KPI widgets that declare refreshSec (travel ops dashboard uses 60s). */
  private async refreshTimedWidgets(): Promise<void> {
    const src = this.effectiveDetail;
    if (!src?.widgets?.length || this.previewMode) {
      return;
    }
    const timed = src.widgets.filter((w) => (w.refreshSec ?? 0) > 0 && this.kind(w) === 'KPI');
    if (!timed.length) {
      return;
    }
    for (const w of timed) {
      await this.loadWidgetData(w);
    }
  }

  private async loadWidgetData(w: WidgetDetailDto): Promise<void> {
    const wid = w.id;
    if (wid == null || wid <= 0) {
      return;
    }
    try {
      const rows = await this.settingsApi.widgetData(wid);
      this.widgetRows.set(wid, rows);
      const apex = this.buildApexOptions(w.widgetType, rows);
      if (apex) {
        this.apexByWidget.set(wid, apex);
      } else {
        this.apexByWidget.delete(wid);
      }
    } catch {
      this.widgetRows.set(wid, []);
      this.apexByWidget.delete(wid);
    }
  }

  private async reloadWidgets(): Promise<void> {
    const src = this.effectiveDetail;
    this.widgetRows.clear();
    this.apexByWidget.clear();
    this.quickActions = [];
    if (!src?.widgets?.length) {
      return;
    }
    for (const w of src.widgets) {
      if ((w.widgetType || '').toUpperCase() === 'QUICK_ACTION') {
        this.parseQuickActions(w.configJson);
      }
    }
    this.widgetsLoading = true;
    try {
      for (const w of src.widgets) {
        const t = (w.widgetType || '').toUpperCase();
        if (t === 'QUICK_ACTION') {
          continue;
        }
        let rows: Record<string, unknown>[] = [];
        const wid = w.id;
        try {
          if (wid != null && wid > 0) {
            rows = await this.settingsApi.widgetData(wid);
          } else if (this.previewMode && w.queryDefId) {
            const q = this.previewQueryDefs.find((x) => x.id === w.queryDefId);
            if (q?.sqlText) {
              rows = await this.settingsApi.executeQueryTest(q.sqlText);
            }
          }
        } catch {
          rows = [];
        }
        this.widgetRows.set(wid, rows);
        const apex = this.buildApexOptions(w.widgetType, rows);
        if (apex) {
          this.apexByWidget.set(wid, apex);
        }
      }
    } finally {
      this.widgetsLoading = false;
    }
  }

  /** Links for a single QUICK_ACTION widget (builder grid tiles hide the global quick-actions bar). */
  quickLinksFor(w: WidgetDetailDto): { label: string; route: string }[] {
    if ((w.widgetType || '').toUpperCase() !== 'QUICK_ACTION') {
      return [];
    }
    const raw = w.configJson?.trim();
    if (!raw) {
      return [];
    }
    try {
      const cfg = JSON.parse(raw) as { actions?: { label: string; route: string }[] };
      return this.filterAccessibleActions(cfg.actions ?? []);
    } catch {
      return [];
    }
  }

  private parseQuickActions(configJson: string | null | undefined): void {
    if (!configJson) {
      return;
    }
    try {
      const cfg = JSON.parse(configJson) as { actions?: { label: string; route: string }[] };
      this.quickActions = this.filterAccessibleActions(cfg.actions ?? []);
    } catch {
      this.quickActions = [];
    }
  }

  private normalizeWidgetType(raw: string | null | undefined): string {
    const u = (raw || '').toUpperCase().trim();
    const aliases: Record<string, string> = {
      LINE: 'LINE_CHART',
      BAR: 'BAR_CHART',
      AREA: 'AREA_CHART',
      PIE: 'PIE_CHART',
      DONUT: 'DONUT_CHART',
      COLUMN: 'BAR_CHART',
      STACKED_BAR: 'STACKED_BAR_CHART',
      HORIZONTAL_BAR: 'HORIZONTAL_BAR_CHART',
      RADAR: 'RADAR_CHART',
      SCATTER: 'SCATTER_CHART',
      BUBBLE: 'BUBBLE_CHART',
      POLAR_AREA: 'POLAR_AREA_CHART',
      RADIAL_BAR: 'RADIAL_BAR_CHART',
      RANGE_BAR: 'RANGE_BAR_CHART',
      CANDLESTICK: 'CANDLESTICK_CHART',
      TREEMAP: 'TREEMAP_CHART',
    };
    return aliases[u] ?? u;
  }

  /**
   * Pick a category axis and one or more numeric series columns.
   * Handles single-column aggregates (uses row index as category).
   */
  private pickLabelAndSeriesKeys(
    rows: Record<string, unknown>[],
    keys: string[]
  ): { labelKey: string; seriesKeys: string[]; categories: string[] } {
    const labelKey =
      keys.find((k) => rows.some((r) => !isNumericLike(r[k]))) ?? keys[0];
    let seriesKeys = keys.filter((k) => k !== labelKey && rows.some((r) => isNumericLike(r[k])));
    if (!seriesKeys.length) {
      if (keys.length === 1) {
        seriesKeys = [keys[0]];
      } else {
        const rest = keys.filter((k) => k !== labelKey);
        seriesKeys = rest.length ? rest : [keys[keys.length - 1]];
      }
    }
    const categories = rows.map((r, i) => {
      if (keys.length === 1) {
        return String(i + 1);
      }
      return String(r[labelKey] ?? i + 1);
    });
    return { labelKey, seriesKeys, categories };
  }

  /** SQL pattern: row label, column label, value (three columns). */
  private buildHeatmapApex(rows: Record<string, unknown>[], keys: string[]): unknown | null {
    if (keys.length < 3) {
      return null;
    }
    const yk = keys[0];
    const xk = keys[1];
    const vk = keys[2];
    const data = rows.map((r) => ({
      x: String(r[xk] ?? ''),
      y: String(r[yk] ?? ''),
      value: parseNumericLoose(r[vk]),
    }));
    return {
      series: [{ name: vk, data }],
      chart: {
        type: 'heatmap',
        height: 380,
        toolbar: { show: false },
        fontFamily: 'inherit',
        foreColor: '#5a6a85',
      },
      dataLabels: { enabled: false },
      plotOptions: {
        heatmap: {
          shadeIntensity: 0.5,
          radius: 2,
          enableShades: true,
        },
      },
      xaxis: { type: 'category' },
      yaxis: { type: 'category' },
      tooltip: { theme: 'light', style: { fontSize: '12px' } },
      colors: ['#5D87FF'],
    };
  }

  private buildApexOptions(widgetType: string, rows: Record<string, unknown>[]): unknown {
    let t = this.normalizeWidgetType(widgetType);
    if (!rows.length || ['KPI', 'TABLE', 'QUICK_ACTION', 'CALENDAR', 'CARD_LIST'].includes(t)) {
      return null;
    }
    const keys = Object.keys(rows[0]);
    if (!keys.length) {
      return null;
    }

    if (t === 'HEATMAP') {
      const hm = this.buildHeatmapApex(rows, keys);
      if (hm) {
        return hm;
      }
      t = 'BAR_CHART';
    }

    if (t === 'SCATTER_CHART') {
      return this.buildScatterApex(rows, keys);
    }
    if (t === 'BUBBLE_CHART') {
      return this.buildBubbleApex(rows, keys);
    }
    if (t === 'RANGE_BAR_CHART') {
      return this.buildRangeBarApex(rows, keys);
    }
    if (t === 'CANDLESTICK_CHART') {
      return this.buildCandlestickApex(rows, keys);
    }
    if (t === 'TREEMAP_CHART') {
      return this.buildTreemapApex(rows, keys);
    }

    const { labelKey, seriesKeys, categories } = this.pickLabelAndSeriesKeys(rows, keys);
    if (!seriesKeys.length) {
      return null;
    }

    if (t === 'POLAR_AREA_CHART') {
      const valKey = seriesKeys[0];
      const series = rows.map((r) => parseNumericLoose(r[valKey]));
      return {
        series,
        labels: categories,
        chart: {
          type: 'polarArea',
          height: 340,
          toolbar: { show: false },
          fontFamily: 'inherit',
          foreColor: '#5a6a85',
        },
        fill: { opacity: 0.85 },
        legend: { position: 'bottom', fontWeight: 500 },
        colors: ['#5D87FF', '#49BEFF', '#13DEB9', '#FA896B', '#FFAE1F', '#539BFF'],
        yaxis: { labels: { style: { fontSize: '11px' } } },
        tooltip: { theme: 'light', style: { fontSize: '12px' } },
      };
    }

    if (t === 'RADIAL_BAR_CHART') {
      const valKey = seriesKeys[0];
      const series = rows.map((r) => Number(r[valKey]) || 0);
      return {
        series,
        chart: {
          type: 'radialBar',
          height: 380,
          toolbar: { show: false },
          fontFamily: 'inherit',
          foreColor: '#5a6a85',
        },
        labels: categories,
        plotOptions: {
          radialBar: {
            hollow: { size: '58%' },
            track: { background: '#f1f1f1' },
            dataLabels: {
              name: { fontSize: '12px' },
              value: { fontSize: '16px', fontWeight: 700 },
            },
          },
        },
        colors: ['#5D87FF', '#49BEFF', '#13DEB9', '#FA896B', '#FFAE1F', '#539BFF'],
        legend: { position: 'bottom', fontWeight: 500 },
        tooltip: { theme: 'light' },
      };
    }

    if (t === 'RADAR_CHART') {
      const series = seriesKeys.map((name) => ({
        name,
        data: rows.map((r) => parseNumericLoose(r[name])),
      }));
      return {
        series,
        chart: {
          type: 'radar',
          height: 400,
          toolbar: { show: false },
          fontFamily: 'inherit',
          foreColor: '#5a6a85',
          animations: { enabled: true },
        },
        xaxis: { categories },
        markers: { size: 4, hover: { size: 6 } },
        stroke: { width: 2 },
        fill: { opacity: 0.12 },
        yaxis: { show: false },
        plotOptions: {
          radar: {
            polygons: {
              strokeColors: 'rgba(0,0,0,0.08)',
              connectorColors: 'rgba(0,0,0,0.08)',
            },
          },
        },
        colors: ['#5D87FF', '#49BEFF', '#13DEB9', '#FA896B'],
        tooltip: { theme: 'light', style: { fontSize: '12px' } },
      };
    }

    if (t === 'PIE_CHART' || t === 'DONUT_CHART') {
      const valKey = seriesKeys[0];
      const series = rows.map((r) => parseNumericLoose(r[valKey]));
      const labels = categories;
      return {
        series,
        labels,
        chart: {
          type: t === 'DONUT_CHART' ? 'donut' : 'pie',
          height: 320,
          toolbar: { show: false },
          fontFamily: 'inherit',
          foreColor: '#5a6a85',
          animations: { enabled: true, speed: 600 },
        },
        stroke: { show: true, width: 2, colors: ['#fff'] },
        colors: ['#5D87FF', '#49BEFF', '#13DEB9', '#FA896B', '#FFAE1F', '#539BFF'],
        legend: { position: 'bottom', fontWeight: 500 },
        dataLabels: { enabled: true, style: { fontSize: '11px' } },
        plotOptions: {
          pie: {
            expandOnClick: true,
            donut: {
              size: t === 'DONUT_CHART' ? '68%' : '0%',
              labels: {
                show: true,
                name: { fontSize: '12px' },
                value: { fontSize: '14px', fontWeight: 600 },
              },
            },
          },
        },
      };
    }

    if (
      t === 'LINE_CHART' ||
      t === 'BAR_CHART' ||
      t === 'AREA_CHART' ||
      t === 'STACKED_BAR_CHART' ||
      t === 'HORIZONTAL_BAR_CHART' ||
      t === 'LINE' ||
      t === 'BAR'
    ) {
      const isStacked = t === 'STACKED_BAR_CHART';
      const isHorizontal = t === 'HORIZONTAL_BAR_CHART';
      const chartType =
        t === 'BAR_CHART' || t === 'BAR' || isStacked || isHorizontal
          ? 'bar'
          : t === 'AREA_CHART'
            ? 'area'
            : 'line';
      const series = seriesKeys.map((name) => ({
        name,
        data: rows.map((r) => parseNumericLoose(r[name])),
      }));
      return {
        series,
        chart: {
          type: chartType,
          height: chartType === 'bar' ? 340 : 360,
          stacked: isStacked ? true : undefined,
          stackType: isStacked ? 'normal' : undefined,
          toolbar: { show: true },
          fontFamily: 'inherit',
          foreColor: '#5a6a85',
          animations: {
            enabled: true,
            easing: 'easeinout',
            speed: 700,
          },
          zoom: { enabled: false },
        },
        dataLabels: { enabled: false },
        stroke: {
          curve: 'smooth',
          width: chartType === 'bar' ? 0 : chartType === 'area' ? 2 : 3,
          lineCap: 'round',
        },
        markers: chartType === 'line' || chartType === 'area' ? { size: 4, hover: { size: 7 } } : { size: 0 },
        xaxis: {
          categories,
          labels: {
            rotate: chartType === 'bar' && !isHorizontal ? 0 : -35,
            style: { colors: '#8a94a6', fontSize: '11px' },
          },
          axisBorder: { show: false },
          axisTicks: { show: false },
        },
        yaxis: {
          labels: { style: { colors: '#8a94a6', fontSize: '11px' } },
        },
        grid: {
          borderColor: 'rgba(0,0,0,0.06)',
          strokeDashArray: 4,
          padding: { left: 8, right: 12 },
        },
        plotOptions:
          chartType === 'bar'
            ? {
                bar: {
                  horizontal: isHorizontal,
                  columnWidth: isHorizontal ? '62%' : '52%',
                  borderRadius: 6,
                  dataLabels: { position: isHorizontal ? 'center' : 'top' },
                },
              }
            : {},
        tooltip: {
          theme: 'light',
          style: { fontSize: '12px' },
          x: { show: true },
        },
        fill:
          chartType === 'area'
            ? {
                type: 'gradient',
                gradient: {
                  shadeIntensity: 1,
                  opacityFrom: 0.45,
                  opacityTo: 0.05,
                  stops: [0, 90, 100],
                },
              }
            : {},
      };
    }

    return null;
  }

  /** Column must look numeric in enough rows (sparse NULLs / JDBC strings). */
  private stableNumericKeys(rows: Record<string, unknown>[], keys: string[]): string[] {
    if (!rows.length) {
      return [];
    }
    const need = Math.max(1, Math.ceil(rows.length * 0.35));
    return keys.filter((k) => rows.filter((r) => isNumericLike(r[k])).length >= need);
  }

  /** Prefer OPEN/HIGH/LOW/CLOSE in column names; otherwise last four stable numeric columns. */
  private resolveOhlcKeys(rows: Record<string, unknown>[], keys: string[]): string[] | null {
    const ku = keys.map((k) => ({ k, u: k.toUpperCase() }));
    const pick = (fn: (u: string) => boolean): string | undefined =>
      ku.find((x) => fn(x.u))?.k;
    const open = pick((u) => /\bOPEN\b|_OPEN$|^OPEN_|^O$/.test(u));
    const high = pick((u) => /\bHIGH\b|_HIGH$|^HIGH_/.test(u));
    const low = pick((u) => /\bLOW\b|_LOW$|^LOW_/.test(u) && !/\bLOWER\b/.test(u));
    const close = pick((u) => /\bCLOSE\b|_CLOSE$|^CLOSE_|ADJ.*CLOSE/.test(u));
    if (open && high && low && close && new Set([open, high, low, close]).size === 4) {
      return [open, high, low, close];
    }
    const nk = this.stableNumericKeys(rows, keys);
    if (nk.length >= 4) {
      return nk.length > 4 ? nk.slice(-4) : nk.slice(0, 4);
    }
    const loose = keys.filter((k) => rows.some((r) => isNumericLike(r[k])));
    if (loose.length >= 4) {
      return loose.length > 4 ? loose.slice(-4) : loose.slice(0, 4);
    }
    return null;
  }

  private buildScatterApex(rows: Record<string, unknown>[], keys: string[]): unknown | null {
    let numericKeys = this.stableNumericKeys(rows, keys);
    if (numericKeys.length >= 2) {
      const xk = numericKeys[0];
      const yk = numericKeys[1];
      const data = rows.map((r) => [parseNumericLoose(r[xk]), parseNumericLoose(r[yk])]);
      return {
        series: [{ name: `${xk} × ${yk}`, data }],
      chart: {
        type: 'scatter',
        height: 380,
        toolbar: { show: true },
        fontFamily: 'inherit',
        foreColor: '#5a6a85',
        zoom: { enabled: true },
      },
      xaxis: {
        type: 'numeric',
        tickAmount: 6,
        labels: { style: { colors: '#8a94a6', fontSize: '11px' } },
      },
      yaxis: {
        tickAmount: 6,
        labels: { style: { colors: '#8a94a6', fontSize: '11px' } },
      },
      grid: { borderColor: 'rgba(0,0,0,0.06)', strokeDashArray: 4 },
      markers: { size: 6 },
      tooltip: { theme: 'light', style: { fontSize: '12px' } },
      colors: ['#5D87FF'],
    };
    }

    if (numericKeys.length === 1) {
      const yk = numericKeys[0];
      const labelKey =
        keys.find((k) => k !== yk) ?? keys[0];
      const labels = rows.map((r) => String(r[labelKey] ?? ''));
      const uniq = [...new Set(labels)];
      const data = rows.map((r, i) => [
        uniq.indexOf(String(r[labelKey] ?? '')) + (uniq.length > 1 ? 0 : i),
        parseNumericLoose(r[yk]),
      ]);
      return {
        series: [{ name: `${labelKey} × ${yk}`, data }],
        chart: {
          type: 'scatter',
          height: 380,
          toolbar: { show: true },
          fontFamily: 'inherit',
          foreColor: '#5a6a85',
          zoom: { enabled: true },
        },
        xaxis: {
          type: 'numeric',
          tickAmount: Math.min(8, Math.max(uniq.length, 2)),
          labels: { style: { colors: '#8a94a6', fontSize: '11px' } },
        },
        yaxis: {
          labels: { style: { colors: '#8a94a6', fontSize: '11px' } },
        },
        grid: { borderColor: 'rgba(0,0,0,0.06)', strokeDashArray: 4 },
        markers: { size: 6 },
        tooltip: { theme: 'light', style: { fontSize: '12px' } },
        colors: ['#5D87FF'],
      };
    }

    return null;
  }

  private buildBubbleApex(rows: Record<string, unknown>[], keys: string[]): unknown | null {
    let numericKeys = this.stableNumericKeys(rows, keys);
    if (numericKeys.length >= 3) {
      const [xk, yk, zk] = numericKeys.slice(0, 3);
      const data = rows.map((r) => [
        parseNumericLoose(r[xk]),
        parseNumericLoose(r[yk]),
        parseNumericLoose(r[zk]),
      ]);
      return {
        series: [{ name: `${xk}, ${yk}, ${zk}`, data }],
      chart: {
        type: 'bubble',
        height: 400,
        toolbar: { show: true },
        fontFamily: 'inherit',
        foreColor: '#5a6a85',
        zoom: { enabled: true },
      },
      xaxis: {
        type: 'numeric',
        labels: { style: { colors: '#8a94a6', fontSize: '11px' } },
      },
      yaxis: {
        labels: { style: { colors: '#8a94a6', fontSize: '11px' } },
      },
      plotOptions: { bubble: { minBubbleRadius: 4, maxBubbleRadius: 28 } },
      fill: { opacity: 0.88 },
      markers: { strokeWidth: 0 },
      tooltip: { theme: 'light', style: { fontSize: '12px' } },
      colors: ['#5D87FF'],
    };
    }

    if (numericKeys.length === 2) {
      const [xk, yk] = numericKeys;
      const data = rows.map((r) => {
        const xv = parseNumericLoose(r[xk]);
        const yv = parseNumericLoose(r[yk]);
        const z = Math.max(8, Math.abs(yv) || Math.abs(xv) || 8);
        return [xv, yv, z];
      });
      return {
        series: [{ name: `${xk}, ${yk}, z≈|y|`, data }],
        chart: {
          type: 'bubble',
          height: 400,
          toolbar: { show: true },
          fontFamily: 'inherit',
          foreColor: '#5a6a85',
          zoom: { enabled: true },
        },
        xaxis: {
          type: 'numeric',
          labels: { style: { colors: '#8a94a6', fontSize: '11px' } },
        },
        yaxis: {
          labels: { style: { colors: '#8a94a6', fontSize: '11px' } },
        },
        plotOptions: { bubble: { minBubbleRadius: 4, maxBubbleRadius: 28 } },
        fill: { opacity: 0.88 },
        markers: { strokeWidth: 0 },
        tooltip: { theme: 'light', style: { fontSize: '12px' } },
        colors: ['#5D87FF'],
      };
    }

    if (numericKeys.length === 1) {
      const yk = numericKeys[0];
      const data = rows.map((r, i) => {
        const yv = parseNumericLoose(r[yk]);
        const z = Math.max(8, Math.abs(yv) || 8);
        return [i + 1, yv, z];
      });
      return {
        series: [{ name: `${yk} (index × value)`, data }],
        chart: {
          type: 'bubble',
          height: 400,
          toolbar: { show: true },
          fontFamily: 'inherit',
          foreColor: '#5a6a85',
          zoom: { enabled: true },
        },
        xaxis: { type: 'numeric', labels: { style: { fontSize: '11px' } } },
        yaxis: { labels: { style: { fontSize: '11px' } } },
        plotOptions: { bubble: { minBubbleRadius: 4, maxBubbleRadius: 28 } },
        fill: { opacity: 0.88 },
        tooltip: { theme: 'light', style: { fontSize: '12px' } },
        colors: ['#5D87FF'],
      };
    }

    return null;
  }

  private buildRangeBarApex(rows: Record<string, unknown>[], keys: string[]): unknown | null {
    let numericKeys = this.stableNumericKeys(rows, keys);
    if (numericKeys.length < 2) {
      return null;
    }
    const lowK = numericKeys[numericKeys.length - 2];
    const highK = numericKeys[numericKeys.length - 1];
    let catKey = keys.find((k) => k !== lowK && k !== highK);
    const data = rows.map((r, i) => ({
      x: catKey ? String(r[catKey] ?? '') : `Row ${i + 1}`,
      y: [parseNumericLoose(r[lowK]), parseNumericLoose(r[highK])],
    }));
    return {
      series: [{ name: `${lowK}–${highK}`, data }],
      chart: {
        type: 'rangeBar',
        height: 380,
        toolbar: { show: false },
        fontFamily: 'inherit',
        foreColor: '#5a6a85',
      },
      plotOptions: {
        bar: {
          horizontal: true,
          barHeight: '72%',
          rangeBarGroupRows: false,
        },
      },
      colors: ['#5D87FF'],
      xaxis: { type: 'category' },
      tooltip: { theme: 'light', style: { fontSize: '12px' } },
      grid: { borderColor: 'rgba(0,0,0,0.06)', strokeDashArray: 4 },
    };
  }

  private buildCandlestickApex(rows: Record<string, unknown>[], keys: string[]): unknown | null {
    const ohlc = this.resolveOhlcKeys(rows, keys);
    if (!ohlc) {
      return null;
    }
    const [openK, highK, lowK, closeK] = ohlc;
    const labelKey =
      keys.find((k) => k !== openK && k !== highK && k !== lowK && k !== closeK) ?? keys[0];
    const data = rows.map((r) => ({
      x: String(r[labelKey] ?? ''),
      y: [
        parseNumericLoose(r[openK]),
        parseNumericLoose(r[highK]),
        parseNumericLoose(r[lowK]),
        parseNumericLoose(r[closeK]),
      ],
    }));
    return {
      series: [{ name: 'OHLC', data }],
      chart: {
        type: 'candlestick',
        height: 400,
        toolbar: { show: true },
        fontFamily: 'inherit',
        foreColor: '#5a6a85',
      },
      xaxis: { type: 'category', labels: { rotate: -35, style: { fontSize: '11px' } } },
      yaxis: { labels: { style: { fontSize: '11px' } } },
      grid: { borderColor: 'rgba(0,0,0,0.06)', strokeDashArray: 4 },
      plotOptions: {
        candlestick: {
          colors: { upward: '#13DEB9', downward: '#FA896B' },
        },
      },
      tooltip: { theme: 'light', style: { fontSize: '12px' } },
    };
  }

  private buildTreemapApex(rows: Record<string, unknown>[], keys: string[]): unknown | null {
    const nk = this.stableNumericKeys(rows, keys);
    const valKey = nk[0];
    if (!valKey) {
      return null;
    }
    let labelKey = keys.find((k) => k !== valKey && rows.some((r) => !isNumericLike(r[k])));
    if (!labelKey) {
      labelKey = keys.find((k) => k !== valKey) ?? keys[0];
    }
    const data = rows.map((r, i) => ({
      x: String(r[labelKey] ?? `Item ${i + 1}`),
      y: parseNumericLoose(r[valKey]),
    }));
    return {
      series: [{ data }],
      chart: {
        type: 'treemap',
        height: 380,
        toolbar: { show: false },
        fontFamily: 'inherit',
        foreColor: '#5a6a85',
      },
      plotOptions: {
        treemap: {
          enableShades: true,
          shadeIntensity: 0.35,
          distributed: true,
          useFillColorAsStroke: false,
        },
      },
      colors: ['#5D87FF', '#49BEFF', '#13DEB9', '#FA896B', '#FFAE1F', '#539BFF'],
      legend: { show: false },
      dataLabels: { enabled: true, style: { fontSize: '11px' } },
      tooltip: { theme: 'light', style: { fontSize: '12px' } },
    };
  }

  apexFor(widgetId: number): unknown {
    return this.apexByWidget.get(widgetId);
  }

  kpiValue(wid: number): string {
    const rows = this.widgetRows.get(wid);
    if (!rows?.length) {
      return '—';
    }
    const first = rows[0];
    const keys = Object.keys(first);
    const v = keys.length ? first[keys[0]] : null;
    return v != null ? String(v) : '—';
  }

  tableColumns(wid: number): string[] {
    const rows = this.widgetRows.get(wid);
    if (!rows?.length) {
      return [];
    }
    return Object.keys(rows[0]);
  }

  rowsFor(wid: number): Record<string, unknown>[] {
    return this.widgetRows.get(wid) ?? [];
  }

  /** Matches static dashboard KPI accent cycling (only among KPI widgets). */
  kpiTone(w: WidgetDetailDto): string {
    const kpis = this.widgets().filter((x) => this.kind(x) === 'KPI');
    const idx = kpis.findIndex((x) => x.id === w.id);
    const i = idx >= 0 ? idx : 0;
    return this.kpiTones[i % this.kpiTones.length];
  }

  kpiBgClass(w: WidgetDetailDto): string {
    return `bg-light-${this.kpiTone(w)}`;
  }

  kind(w: WidgetDetailDto): string {
    return (w.widgetType || '').toUpperCase();
  }

  /** Widget type for display — underscores replaced with spaces (e.g. LINE_CHART → LINE CHART). */
  kindLabel(w: WidgetDetailDto): string {
    const raw = (w.widgetType || '').trim();
    return raw ? raw.replace(/_/g, ' ') : '';
  }

  showChart(w: WidgetDetailDto): boolean {
    const k = this.kind(w);
    if (k === 'CALENDAR' || k === 'CARD_LIST') {
      return false;
    }
    return this.apexByWidget.has(w.id);
  }

  showCalendar(w: WidgetDetailDto): boolean {
    return this.kind(w) === 'CALENDAR';
  }

  showCardList(w: WidgetDetailDto): boolean {
    return this.kind(w) === 'CARD_LIST';
  }

  calendarConfig(w: WidgetDetailDto): DashboardCalendarConfig | null {
    return this.parseWidgetConfig<DashboardCalendarConfig>(w.configJson);
  }

  cardListConfig(w: WidgetDetailDto): DashboardCardListConfig | null {
    return this.parseWidgetConfig<DashboardCardListConfig>(w.configJson);
  }

  tableLinkRoute(w: WidgetDetailDto): string | null {
    const cfg = this.parseWidgetConfig<{ linkRoute?: string }>(w.configJson);
    const r = cfg?.linkRoute?.trim();
    return r || null;
  }

  tableLinkLabel(w: WidgetDetailDto): string {
    const cfg = this.parseWidgetConfig<{ linkLabel?: string }>(w.configJson);
    return cfg?.linkLabel?.trim() || 'View';
  }

  tableColumnsFor(wid: number, w: WidgetDetailDto): string[] {
    const cols = this.tableColumns(wid);
    if (this.tableLinkRoute(w)) {
      return [...cols, '__action'];
    }
    return cols;
  }

  showGrid(w: WidgetDetailDto): boolean {
    if (this.kind(w) === 'KPI' || this.showCalendar(w) || this.showCardList(w)) {
      return false;
    }
    if (this.showChart(w)) {
      return false;
    }
    /** Includes chart-only widget types when Apex options are missing — raw SQL grid as fallback. */
    return (this.widgetRows.get(w.id)?.length ?? 0) > 0;
  }

  private parseWidgetConfig<T>(json: string | null | undefined): T | null {
    if (!json?.trim()) {
      return null;
    }
    try {
      return JSON.parse(json) as T;
    } catch {
      return null;
    }
  }
}
