import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { firstValueFrom, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, finalize, takeUntil } from 'rxjs/operators';

import { QueryDefDto } from 'src/app/core/models/settings.models';
import { GetRoleResponse, GetUserResponse } from 'src/app/core/models/um.models';
import { UmRoleService } from 'src/app/pages/um/services/um-role.service';
import { UmUserService } from 'src/app/pages/um/services/um-user.service';
import { MenuCatalogService } from 'src/app/services/menu-catalog.service';
import { SettingsApiService } from 'src/app/services/settings-api.service';

import { ReportingService } from '../reporting.service';
import {
  ReportBuilderItem,
  ReportBuilderSaveRequest,
  ReportFilterConfig,
  ReportFilterType,
  ReportStatus,
} from '../reporting.types';

/**
 * Report Builder admin UI. Two-card layout mirroring Query Builder:
 *
 * <ul>
 *   <li>Left card — new / edit form with name, description, linked Query Builder
 *       query, icon, status, and filter editor.</li>
 *   <li>Right card — paginated list of saved reports with edit / delete /
 *       toggle-status actions.</li>
 * </ul>
 *
 * Columns are auto-detected from the linked query at save time (the server
 * runs the query with all filter binds set to NULL and reads
 * {@code ResultSetMetaData}). The detected columns come back in the save
 * response so the user can immediately see what the report will produce.
 */
@Component({
  selector: 'app-report-builder',
  templateUrl: './report-builder.component.html',
  styleUrls: ['./report-builder.component.scss'],
})
export class ReportBuilderComponent implements OnInit, OnDestroy {
  // ---- Form state ----
  editingId: number | null = null;
  name = '';
  code = '';
  description = '';
  icon = 'report';
  status: ReportStatus = 'ACTIVE';
  queryDefId: number | null = null;
  sortOrder = 0;
  defaultSortKey: string | null = null;
  defaultSortDir: 'ASC' | 'DESC' = 'DESC';
  filters: ReportFilterConfig[] = [];

  /** When the user wants the server to auto-detect columns on save. */
  autoDetectColumns = true;

  /** Read-only echo of detected columns for the last saved report. */
  detectedColumns: { key: string; label: string }[] = [];

  // ---- Visibility (mirrors Dashboard Builder) ----
  /** Selected role NAMES to grant (server resolves to role_type). Empty = no role restriction. */
  selectedGrantRoles: string[] = [];
  /** Full role catalog for the multi-select. */
  allRoles: GetRoleResponse[] = [];

  /** Selected usernames to grant. Empty + empty role grants = "global" within the tenant. */
  selectedUsernames: string[] = [];
  userCatalog: GetUserResponse[] = [];
  filteredUsersForPick: GetUserResponse[] = [];
  userSearchTerm = '';
  loadingUsers = false;
  readonly userPageSize = 100;
  readonly maxUserPagesToPrefetch = 25;

  // ---- List state ----
  queries: QueryDefDto[] = [];
  loadingQueries = false;

  reports: ReportBuilderItem[] = [];
  reportsTotal = 0;
  reportsPageNumber = 0;
  reportsPageSize = 10;
  reportsPageSizeOptions = [5, 10, 25, 50];
  loadingReports = false;
  readonly nameSearchCtrl = new FormControl<string>('', { nonNullable: true });

  saving = false;
  removing = false;
  readonly availableIcons = [
    'report',
    'chart-bar',
    'chart-line',
    'chart-pie',
    'list-search',
    'cash',
    'building-store',
    'calendar-event',
    'shield-lock',
    'users',
    'file-analytics',
    'database',
  ];
  readonly filterTypes: ReportFilterType[] = [
    'DATE_RANGE',
    'DATE',
    'TEXT',
    'USER',
    'SELECT',
    'NUMBER',
  ];

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly reporting: ReportingService,
    private readonly settingsApi: SettingsApiService,
    private readonly menuCatalog: MenuCatalogService,
    private readonly snack: MatSnackBar,
    private readonly router: Router,
    private readonly umRoleService: UmRoleService,
    private readonly umUserService: UmUserService
  ) {}

  ngOnInit(): void {
    this.loadQueries();
    this.loadReports();
    // Fire-and-forget; visibility pickers stay empty if UM is offline (we just lose role/user choices).
    void this.loadAllRoles();
    void this.prefetchUsers();
    this.nameSearchCtrl.valueChanges
      .pipe(debounceTime(250), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(() => {
        this.reportsPageNumber = 0;
        this.loadReports();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // ---------- Loaders ----------

  private loadQueries(): void {
    this.loadingQueries = true;
    void this.settingsApi
      .listAllQueryDefs()
      .then((all) => {
        this.queries = all ?? [];
      })
      .catch(() => {
        this.queries = [];
      })
      .finally(() => {
        this.loadingQueries = false;
      });
  }

  loadReports(): void {
    this.loadingReports = true;
    this.reporting
      .builderList({
        pageNumber: this.reportsPageNumber,
        pageSize: this.reportsPageSize,
        nameSearch: this.nameSearchCtrl.value || null,
      })
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.loadingReports = false))
      )
      .subscribe({
        next: (page) => {
          this.reports = page?.items ?? [];
          this.reportsTotal = page?.totalCount ?? 0;
        },
        error: () => {
          this.reports = [];
          this.reportsTotal = 0;
        },
      });
  }

  onReportsPageChange(ev: PageEvent): void {
    this.reportsPageNumber = ev.pageIndex;
    this.reportsPageSize = ev.pageSize;
    this.loadReports();
  }

  // ---------- Form actions ----------

  resetForm(): void {
    this.editingId = null;
    this.name = '';
    this.code = '';
    this.description = '';
    this.icon = 'report';
    this.status = 'ACTIVE';
    this.queryDefId = null;
    this.sortOrder = 0;
    this.defaultSortKey = null;
    this.defaultSortDir = 'DESC';
    this.filters = [];
    this.autoDetectColumns = true;
    this.detectedColumns = [];
    this.selectedGrantRoles = [];
    this.selectedUsernames = [];
    this.userSearchTerm = '';
    this.applyUserFilter();
  }

  edit(r: ReportBuilderItem): void {
    this.editingId = r.id ?? null;
    this.name = r.name ?? '';
    this.code = r.code ?? '';
    this.description = r.description ?? '';
    this.icon = r.icon ?? 'report';
    this.status = (r.status as ReportStatus) ?? 'ACTIVE';
    this.queryDefId = r.queryDefId ?? null;
    this.sortOrder = r.sortOrder ?? 0;
    this.defaultSortKey = r.defaultSortKey ?? null;
    this.defaultSortDir = (r.defaultSortDir as 'ASC' | 'DESC') ?? 'DESC';
    this.filters = (r.filters ?? []).map((f) => ({ ...f }));
    this.autoDetectColumns = false;
    this.detectedColumns = (r.columns ?? []).map((c) => ({ key: c.key, label: c.label ?? c.key }));
    this.selectedGrantRoles = [...(r.grantRoles ?? [])];
    this.selectedUsernames = [...(r.grantUsernames ?? [])];
    this.userSearchTerm = '';
    this.applyUserFilter();
  }

  canSave(): boolean {
    return (
      !this.saving &&
      !!this.name?.trim() &&
      this.queryDefId != null &&
      this.queryDefId > 0
    );
  }

  save(): void {
    if (!this.canSave()) return;
    const req: ReportBuilderSaveRequest = {
      id: this.editingId ?? undefined,
      name: this.name.trim(),
      code: this.code?.trim() || null,
      description: this.description?.trim() || null,
      icon: this.icon || null,
      status: this.status,
      queryDefId: this.queryDefId!,
      filters: this.filters
        .filter((f) => !!f.key && !!f.type)
        .map((f) => ({
          ...f,
          options:
            f.type === 'SELECT' ? (f.options ?? []).filter((o) => !!o.value) : null,
        })),
      defaultSortKey: this.defaultSortKey || null,
      defaultSortDir: this.defaultSortDir,
      sortOrder: this.sortOrder ?? 0,
      autoDetectColumns: this.autoDetectColumns,
      grantRoles: this.selectedGrantRoles.filter(Boolean),
      grantUsernames: this.selectedUsernames.filter(Boolean),
    };
    this.saving = true;
    this.reporting
      .builderSave(req)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.saving = false))
      )
      .subscribe({
        next: (saved) => {
          this.snack.open(`Report "${saved.name}" saved.`, 'Dismiss', { duration: 2500 });
          this.detectedColumns = (saved.columns ?? []).map((c) => ({
            key: c.key,
            label: c.label ?? c.key,
          }));
          this.editingId = saved.id ?? null;
          this.code = saved.code ?? this.code;
          this.loadReports();
          void this.menuCatalog.reload();
        },
        error: () => {
          // Snackbar handled by envelope notify mode.
        },
      });
  }

  remove(r: ReportBuilderItem): void {
    if (!r.id) return;
    const ok = window.confirm(`Delete report "${r.name}"?`);
    if (!ok) return;
    this.removing = true;
    this.reporting
      .builderDelete(r.id)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.removing = false))
      )
      .subscribe({
        next: () => {
          this.snack.open(`Report "${r.name}" deleted.`, 'Dismiss', { duration: 2500 });
          if (this.editingId === r.id) {
            this.resetForm();
          }
          this.loadReports();
          void this.menuCatalog.reload();
        },
        error: () => {
          // Envelope handles errors.
        },
      });
  }

  openRunner(r: ReportBuilderItem): void {
    if (!r.id) return;
    this.router.navigate(['/reporting/run', r.id]);
  }

  openRunnerForEditing(): void {
    if (this.editingId == null) return;
    this.router.navigate(['/reporting/run', this.editingId]);
  }

  // ---------- Filter editor ----------

  addFilter(): void {
    this.filters = [
      ...this.filters,
      {
        key: 'filter' + (this.filters.length + 1),
        label: '',
        type: 'TEXT',
        paramName: '',
        fromParamName: null,
        toParamName: null,
        placeholder: null,
        options: null,
      },
    ];
  }

  removeFilter(idx: number): void {
    this.filters = this.filters.filter((_, i) => i !== idx);
  }

  moveFilter(idx: number, dir: -1 | 1): void {
    const target = idx + dir;
    if (target < 0 || target >= this.filters.length) return;
    const arr = [...this.filters];
    const tmp = arr[idx];
    arr[idx] = arr[target];
    arr[target] = tmp;
    this.filters = arr;
  }

  onFilterTypeChange(f: ReportFilterConfig): void {
    if (f.type === 'DATE_RANGE') {
      if (!f.fromParamName) f.fromParamName = f.key + 'From';
      if (!f.toParamName) f.toParamName = f.key + 'To';
      f.paramName = null;
      f.options = null;
    } else if (f.type === 'SELECT') {
      f.options = f.options ?? [];
      f.fromParamName = null;
      f.toParamName = null;
      if (!f.paramName) f.paramName = f.key;
    } else {
      f.fromParamName = null;
      f.toParamName = null;
      f.options = null;
      if (!f.paramName) f.paramName = f.key;
    }
  }

  addOption(f: ReportFilterConfig): void {
    if (!f.options) f.options = [];
    f.options.push({ value: '', label: '' });
  }

  removeOption(f: ReportFilterConfig, optIdx: number): void {
    f.options = (f.options ?? []).filter((_, i) => i !== optIdx);
  }

  trackByReportId(_: number, r: ReportBuilderItem): number {
    return r.id ?? 0;
  }
  trackByFilterIndex(idx: number): number {
    return idx;
  }
  trackByOptionIndex(idx: number): number {
    return idx;
  }

  // ---------- Visibility helpers (mirrors DashboardbuilderComponent) ----------

  private async loadAllRoles(): Promise<void> {
    try {
      const acc: GetRoleResponse[] = [];
      let page = 0;
      const pageSize = 200;
      // eslint-disable-next-line no-constant-condition
      while (true) {
        const res = await firstValueFrom(this.umRoleService.gets({ pageNumber: page, pageSize }));
        const items = res.items ?? [];
        acc.push(...items);
        if (items.length < pageSize) break;
        page++;
      }
      this.allRoles = acc.sort((a, b) => a.name.localeCompare(b.name));
    } catch {
      // UM optional — leave allRoles empty so the picker shows "no options" rather than crashing.
      this.allRoles = [];
    }
  }

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
        if (items.length < this.userPageSize) break;
      }
      this.userCatalog = acc;
      this.applyUserFilter();
    } catch {
      this.userCatalog = [];
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
    const t = (this.userSearchTerm ?? '').trim().toLowerCase();
    const pool = this.userCatalog.filter((u) => !this.selectedUsernames.includes(u.username));
    if (!t) {
      this.filteredUsersForPick = pool.slice(0, 60);
      return;
    }
    this.filteredUsersForPick = pool
      .filter((u) => `${u.username} ${u.email} ${u.firstName} ${u.lastName}`.toLowerCase().includes(t))
      .slice(0, 80);
  }

  onUserPicked(event: MatAutocompleteSelectedEvent): void {
    const u = event.option.value as GetUserResponse;
    if (u?.username && !this.selectedUsernames.includes(u.username)) {
      this.selectedUsernames = [...this.selectedUsernames, u.username];
    }
    this.userSearchTerm = '';
    this.applyUserFilter();
  }

  removeUsername(name: string): void {
    this.selectedUsernames = this.selectedUsernames.filter((x) => x !== name);
    this.applyUserFilter();
  }
}
