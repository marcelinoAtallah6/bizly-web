import { Component, OnDestroy, OnInit } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';

import {
  exportCsv,
  exportExcel,
  exportPdf,
} from '../reporting-export.helper';
import { ReportingService } from '../reporting.service';
import {
  ReportColumnDef,
  ReportDateRangeValue,
  ReportFilterDef,
  ReportFilterValue,
  ReportPageResponse,
  ReportRequest,
  ReportTypeMeta,
} from '../reporting.types';

type SortDir = 'ASC' | 'DESC';

interface SortState {
  key: string | null;
  dir: SortDir;
}

/**
 * Runs one user-defined report. The report definition (filters, columns,
 * default sort) is fetched from {@code /reporting/getTypes} keyed by the
 * report's stable {@code code}; we receive the report id from the route
 * and translate to the code by listing the active reports (lightweight
 * call, cached by the menu catalog).
 *
 * The filter widgets are built dynamically from {@link ReportTypeMeta.filters}.
 * Dates use {@code <mat-datepicker>} (calendar pop-up), matching the rest of
 * the portal — no native browser date inputs.
 */
@Component({
  selector: 'app-report-run',
  templateUrl: './report-run.component.html',
  styleUrls: ['./report-run.component.scss'],
})
export class ReportRunComponent implements OnInit, OnDestroy {
  meta: ReportTypeMeta | null = null;
  page: ReportPageResponse | null = null;
  displayColumns: ReportColumnDef[] = [];
  /** Keys of columns the user currently has visible. */
  visibleColumnKeys: string[] = [];

  filters: Record<string, ReportFilterValue> = {};
  sort: SortState = { key: null, dir: 'DESC' };

  pageNumber = 0;
  pageSize = 25;
  pageSizeOptions = [10, 25, 50, 100, 200];

  loading = false;
  loadingExport = false;
  errorMessage: string | null = null;

  private reportId: number | null = null;
  private reportCode: string | null = null;
  private readonly destroy$ = new Subject<void>();

  /** Read-only viewer under /reports; admin builder uses /reporting. */
  get viewerMode(): boolean {
    if (this.route.snapshot.data['viewerMode'] === true) {
      return true;
    }
    let snap: ActivatedRouteSnapshot | null = this.route.snapshot;
    while (snap) {
      if (snap.data['viewerMode'] === true) {
        return true;
      }
      snap = snap.parent;
    }
    return this.router.url.split('?')[0].startsWith('/reports');
  }

  constructor(
    private readonly reporting: ReportingService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      const idParam = params.get('id');
      const parsed = idParam ? Number(idParam) : NaN;
      if (!Number.isFinite(parsed) || parsed <= 0) {
        this.errorMessage = 'Missing report id.';
        return;
      }
      this.reportId = parsed;
      this.bootstrap();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private bootstrap(): void {
    if (this.reportId == null) return;
    this.loading = true;
    this.errorMessage = null;
    this.meta = null;
    this.page = null;

    // Resolve id → code from the active list, then load full meta from getTypes
    // so the runner stays consistent with how /reporting/generate looks up the report.
    const list$ = this.viewerMode
      ? this.reporting.listAssignedReports()
      : this.reporting.builderListActive();
    list$
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (active) => {
          const ref = (active || []).find((r) => r.id === this.reportId);
          if (!ref) {
            this.loading = false;
            this.errorMessage = 'This report no longer exists or is not active.';
            return;
          }
          this.reportCode = ref.code;
          this.reporting
            .getTypes()
            .pipe(
              takeUntil(this.destroy$),
              finalize(() => (this.loading = false))
            )
            .subscribe({
              next: (types) => {
                const found = types.find((t) => t.key === ref.code);
                if (!found) {
                  this.errorMessage = 'Report metadata could not be loaded.';
                  return;
                }
                this.meta = found;
                this.applyMeta(found);
                this.runGenerate();
              },
              error: (err) => {
                this.errorMessage = this.readError(err);
              },
            });
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage = this.readError(err);
        },
      });
  }

  private applyMeta(meta: ReportTypeMeta): void {
    this.displayColumns = meta.columns ?? [];
    this.visibleColumnKeys = this.displayColumns.map((c) => c.key);
    this.sort = {
      key: meta.defaultSortKey ?? null,
      dir: meta.defaultSortDir ?? 'DESC',
    };
    this.filters = {};
    this.pageNumber = 0;
  }

  // ---------- Filter helpers ----------

  getTextValue(key: string): string {
    const v = this.filters[key];
    return typeof v === 'string' ? v : '';
  }
  setTextValue(key: string, value: string): void { this.filters[key] = value; }

  getNumberValue(key: string): number | null {
    const v = this.filters[key];
    return typeof v === 'number' ? v : null;
  }
  setNumberValue(key: string, value: number | null): void { this.filters[key] = value; }

  getSelectValue(key: string): string {
    const v = this.filters[key];
    return typeof v === 'string' ? v : '';
  }
  setSelectValue(key: string, value: string): void { this.filters[key] = value || null; }

  getDateValue(key: string): Date | null {
    const v = this.filters[key];
    if (typeof v === 'string' && v) {
      const d = new Date(v);
      return Number.isNaN(d.getTime()) ? null : d;
    }
    return null;
  }
  setDateValue(key: string, value: Date | null): void {
    this.filters[key] = value ? this.toIsoDate(value) : null;
  }

  getDateRange(key: string): ReportDateRangeValue {
    const v = this.filters[key];
    if (v && typeof v === 'object' && !Array.isArray(v)) {
      return v as ReportDateRangeValue;
    }
    return {};
  }
  getDateRangeFrom(key: string): Date | null {
    const s = this.getDateRange(key).from;
    return s ? new Date(s) : null;
  }
  getDateRangeTo(key: string): Date | null {
    const s = this.getDateRange(key).to;
    return s ? new Date(s) : null;
  }
  setDateRangeFrom(key: string, value: Date | null): void {
    const current = this.getDateRange(key);
    this.filters[key] = {
      from: value ? this.toIsoDate(value) : null,
      to: current.to ?? null,
    };
  }
  setDateRangeTo(key: string, value: Date | null): void {
    const current = this.getDateRange(key);
    this.filters[key] = {
      from: current.from ?? null,
      to: value ? this.toIsoDate(value) : null,
    };
  }

  private toIsoDate(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  resetFilters(): void {
    this.filters = {};
    this.pageNumber = 0;
    this.runGenerate();
  }

  // ---------- Column visibility ----------

  isColumnVisible(key: string): boolean {
    return this.visibleColumnKeys.includes(key);
  }
  toggleColumnVisibility(key: string): void {
    if (this.isColumnVisible(key)) {
      // Don't allow hiding the last visible column.
      if (this.visibleColumnKeys.length <= 1) return;
      this.visibleColumnKeys = this.visibleColumnKeys.filter((k) => k !== key);
    } else {
      const ordered = this.displayColumns
        .map((c) => c.key)
        .filter((k) => this.visibleColumnKeys.includes(k) || k === key);
      this.visibleColumnKeys = ordered;
    }
  }
  showAllColumns(): void {
    this.visibleColumnKeys = this.displayColumns.map((c) => c.key);
  }
  get visibleColumns(): ReportColumnDef[] {
    return this.displayColumns.filter((c) => this.visibleColumnKeys.includes(c.key));
  }

  // ---------- Pagination + sort ----------

  onPageChange(ev: PageEvent): void {
    this.pageNumber = ev.pageIndex;
    this.pageSize = ev.pageSize;
    this.runGenerate();
  }

  toggleSort(col: ReportColumnDef): void {
    if (!col.sortable) return;
    if (this.sort.key === col.key) {
      this.sort.dir = this.sort.dir === 'ASC' ? 'DESC' : 'ASC';
    } else {
      this.sort = { key: col.key, dir: 'ASC' };
    }
    this.pageNumber = 0;
    this.runGenerate();
  }

  sortIcon(col: ReportColumnDef): string {
    if (!col.sortable) return '';
    if (this.sort.key !== col.key) return 'selector';
    return this.sort.dir === 'ASC' ? 'arrow-up' : 'arrow-down';
  }

  // ---------- Generate + export ----------

  runGenerate(): void {
    if (!this.reportCode) return;
    const req = this.buildRequest(this.pageNumber, this.pageSize);
    this.loading = true;
    this.errorMessage = null;
    this.reporting
      .generate(req)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: (resp) => {
          this.page = resp ?? null;
          if (resp?.columns?.length) {
            this.displayColumns = resp.columns;
            const existing = new Set(this.visibleColumnKeys);
            const fresh = resp.columns.map((c) => c.key);
            if (!fresh.every((k) => existing.has(k))) {
              this.visibleColumnKeys = fresh;
            }
          }
        },
        error: (err) => {
          this.page = null;
          this.errorMessage = this.readError(err);
        },
      });
  }

  exportTo(kind: 'csv' | 'xlsx' | 'pdf'): void {
    if (!this.reportCode || !this.meta) return;
    const req = this.buildRequest(0, 0);
    req.maxRows = 10_000;
    this.loadingExport = true;
    this.reporting
      .export(req)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loadingExport = false))
      )
      .subscribe({
        next: (resp) => {
          if (!resp || !resp.items?.length) {
            this.snack.open('Nothing to export for the current filters.', 'Dismiss', { duration: 3000 });
            return;
          }
          const cols = (resp.columns?.length ? resp.columns : this.displayColumns).filter((c) =>
            this.isColumnVisible(c.key)
          );
          const name = this.meta?.name ?? 'Report';
          try {
            if (kind === 'csv') exportCsv(name, cols, resp.items);
            if (kind === 'xlsx') exportExcel(name, cols, resp.items);
            if (kind === 'pdf') exportPdf(name, cols, resp.items);
          } catch {
            this.snack.open('Could not build the export file.', 'Dismiss', { duration: 4000 });
          }
        },
        error: (err) => {
          this.snack.open(this.readError(err) ?? 'Export failed.', 'Dismiss', { duration: 4000 });
        },
      });
  }

  goBack(): void {
    this.router.navigateByUrl(this.viewerMode ? '/reports' : '/reporting');
  }

  // ---------- Track / cell render ----------

  trackByRowIndex(index: number): number { return index; }
  trackByColKey(_: number, col: ReportColumnDef): string { return col.key; }
  trackByFilterKey(_: number, f: ReportFilterDef): string { return f.key; }

  renderCell(row: Record<string, unknown>, col: ReportColumnDef): string {
    const v = row[col.key];
    if (v === null || v === undefined || v === '') return '';
    if (col.type === 'DATE' || col.type === 'DATETIME') {
      const d = new Date(String(v));
      if (!Number.isNaN(d.getTime())) {
        return col.type === 'DATE' ? d.toLocaleDateString() : d.toLocaleString();
      }
    }
    if (col.type === 'MONEY') {
      const n = Number(v);
      if (!Number.isNaN(n)) {
        return n.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
      }
    }
    if (col.type === 'BOOLEAN') return v ? 'Yes' : 'No';
    return String(v);
  }

  // ---------- Helpers ----------

  private buildRequest(pageNumber: number, pageSize: number): ReportRequest {
    const cleaned: Record<string, ReportFilterValue> = {};
    for (const [k, v] of Object.entries(this.filters)) {
      if (v === null || v === undefined) continue;
      if (typeof v === 'string' && v.trim() === '') continue;
      if (typeof v === 'object' && !Array.isArray(v)) {
        const r = v as ReportDateRangeValue;
        if (!r.from && !r.to) continue;
      }
      cleaned[k] = v;
    }
    return {
      typeKey: this.reportCode!,
      filters: cleaned,
      pageNumber,
      pageSize,
      sortBy: this.sort.key ?? undefined,
      sortDir: this.sort.dir,
    };
  }

  private readError(err: unknown): string | null {
    if (!err) return null;
    if (err instanceof Error) return err.message;
    if (typeof err === 'object' && err !== null && 'message' in err) {
      return String((err as { message: unknown }).message);
    }
    return null;
  }
}
