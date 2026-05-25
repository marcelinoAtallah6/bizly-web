import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ColDef, GridApi, GridReadyEvent } from 'ag-grid-community';
import { forkJoin, finalize } from 'rxjs';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import {
  UmWorkflowService,
  WorkflowCatalogScreen,
  WorkflowInstanceRow,
  WorkflowQueueQuery,
} from '../../services/um-workflow.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import { WorkflowQueueActionsRendererComponent } from './workflow-queue-actions-renderer.component';

@Component({
  selector: 'app-workflow-reject-dialog',
  template: `
    <h2 mat-dialog-title>Reject request</h2>
    <mat-dialog-content [formGroup]="form">
      <mat-form-field appearance="outline" class="full">
        <mat-label>Comment</mat-label>
        <textarea matInput rows="3" formControlName="comment" placeholder="Reason for rejection (optional)"></textarea>
        <mat-hint align="end">{{ form.get('comment')?.value?.length || 0 }} / 4000</mat-hint>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button type="button" (click)="dialogRef.close(undefined)">Cancel</button>
      <button mat-flat-button color="warn" type="button" (click)="submit()">Reject</button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .full {
        width: 100%;
      }
      mat-dialog-content {
        min-width: 320px;
      }
    `,
  ],
})
export class WorkflowRejectDialogComponent {
  form: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    readonly dialogRef: MatDialogRef<WorkflowRejectDialogComponent, string | undefined>,
    @Inject(MAT_DIALOG_DATA) _data: unknown
  ) {
    this.form = this.fb.group({
      comment: ['', [Validators.maxLength(4000)]],
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const c = (this.form.get('comment')?.value as string)?.trim();
    this.dialogRef.close(c ?? '');
  }
}

@Component({
  template: `
    <h2 mat-dialog-title>Request details</h2>
    <mat-dialog-content class="payload-dlg">
      <dl class="kv" *ngIf="entries.length">
        <ng-container *ngFor="let e of entries">
          <dt>{{ e.field }}</dt>
          <dd>{{ e.value }}</dd>
        </ng-container>
      </dl>
      <pre *ngIf="!entries.length" class="json">{{ formatted }}</pre>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-stroked-button mat-dialog-close type="button">Close</button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .payload-dlg {
        min-width: 320px;
        max-width: 520px;
      }
      .kv {
        margin: 0;
      }
      .kv dt {
        font-weight: 600;
        margin-top: 10px;
        font-size: 12px;
        color: rgba(0, 0, 0, 0.55);
      }
      .kv dd {
        margin: 4px 0 0;
        font-size: 14px;
        word-break: break-word;
      }
      .json {
        max-height: 360px;
        overflow: auto;
        font-size: 12px;
        margin: 0;
      }
    `,
  ],
})
export class WorkflowPayloadDialogComponent {
  formatted = '';
  entries: { field: string; value: string }[] = [];

  constructor(@Inject(MAT_DIALOG_DATA) data: { json: string }) {
    const raw = data.json ?? '';
    try {
      const o = JSON.parse(raw || '{}') as unknown;
      if (o != null && typeof o === 'object' && !Array.isArray(o)) {
        this.entries = Object.keys(o as Record<string, unknown>).map((field) => {
          const val = (o as Record<string, unknown>)[field];
          let value: string;
          if (val === null || val === undefined) {
            value = '';
          } else if (typeof val === 'object') {
            value = JSON.stringify(val);
          } else {
            value = String(val);
          }
          return { field, value };
        });
        this.formatted = JSON.stringify(o, null, 2);
        return;
      }
    } catch {
      /* fall through */
    }
    this.formatted = raw;
  }
}

@Component({
  selector: 'app-workflow-queue',
  templateUrl: './workflow-queue.component.html',
  styleUrls: ['./workflow-queue.component.scss'],
})
export class WorkflowQueueComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  loading = false;
  rowData: WorkflowInstanceRow[] = [];
  catalog: WorkflowCatalogScreen[] = [];
  mutatingVerbs: string[] = [];
  /** Actions present in the current result set (or permission verbs as fallback). */
  actionFilterOptions: string[] = [];
  statusFilter = '';
  screenRouteFilter = '';
  actionFilter = '';
  dateFromDate: Date | null = null;
  dateToDate: Date | null = null;
  actingId: number | null = null;
  private gridApi: GridApi<WorkflowInstanceRow> | null = null;

  readonly statusOptions = [
    { value: '', label: 'All' },
    { value: 'PENDING', label: 'Pending' },
    { value: 'APPROVED', label: 'Approved' },
    { value: 'REJECTED', label: 'Rejected' },
  ];

  columnDefs: ColDef<WorkflowInstanceRow>[] = [];

  defaultColDef: ColDef = {
    resizable: true,
    floatingFilter: false,
  };

  constructor(
    private readonly workflow: UmWorkflowService,
    private readonly dialog: MatDialog
  ) {}

  get toolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
    ];
  }

  private toYmd(d: Date | null): string | undefined {
    if (!d) {
      return undefined;
    }
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }

  ngOnInit(): void {
    forkJoin({
      catalog: this.workflow.catalogScreens(),
      verbs: this.workflow.mutatingActions(),
    }).subscribe({
      next: ({ catalog, verbs }) => {
        this.catalog = catalog ?? [];
        this.mutatingVerbs = verbs ?? [];
        this.refreshActionFilterOptions();
      },
      error: () => {
        this.catalog = [];
        this.mutatingVerbs = [];
        this.refreshActionFilterOptions();
      },
    });
    this.buildColumns();
    this.load();
  }

  screenLabel(s: WorkflowCatalogScreen): string {
    const t = s.menuLabel || s.route;
    return s.route ? `${t} — ${s.route}` : t;
  }

  private buildColumns(): void {
    this.columnDefs = [
      { field: 'id', headerName: 'Request ID', width: 110, sortable: true, filter: true },
      { field: 'screenName', headerName: 'Screen', flex: 1, minWidth: 140, sortable: true, filter: true },
      { field: 'actionName', headerName: 'Action', width: 110, sortable: true, filter: true },
      {
        field: 'triggeredByUsername',
        headerName: 'Created by',
        width: 140,
        sortable: true,
        filter: true,
      },
      {
        field: 'createdAt',
        headerName: 'Created date',
        width: 180,
        sortable: true,
        filter: true,
        valueFormatter: (p) => {
          if (!p.value) return '';
          try {
            return new Date(p.value as string).toLocaleString();
          } catch {
            return String(p.value);
          }
        },
      },
      { field: 'status', headerName: 'Status', width: 120, sortable: true, filter: true },
      {
        field: 'checkerComment',
        headerName: 'Checker notes',
        flex: 1,
        minWidth: 140,
        sortable: true,
        filter: true,
        valueFormatter: (p) => (p.value ? String(p.value) : ''),
      },
      {
        headerName: 'Actions',
        width: 148,
        pinned: 'right',
        sortable: false,
        filter: false,
        cellRenderer: WorkflowQueueActionsRendererComponent,
      },
    ];
  }

  onGridReady(e: GridReadyEvent<WorkflowInstanceRow>): void {
    this.gridApi = e.api;
  }

  private refreshActionFilterOptions(): void {
    const fromRows = new Set<string>();
    for (const row of this.rowData) {
      const a = row.actionName?.trim();
      if (a) {
        fromRows.add(a.toUpperCase());
      }
    }
    if (fromRows.size > 0) {
      this.actionFilterOptions = Array.from(fromRows).sort();
      return;
    }
    this.actionFilterOptions = (this.mutatingVerbs ?? [])
      .map((x) => (x ?? '').trim().toUpperCase())
      .filter((x) => !!x)
      .sort();
  }

  load(): void {
    this.loading = true;
    const q: WorkflowQueueQuery = {
      status: this.statusFilter || undefined,
      screenRoute: this.screenRouteFilter.trim() || undefined,
      actionName: this.actionFilter.trim() || undefined,
      dateFrom: this.toYmd(this.dateFromDate),
      dateTo: this.toYmd(this.dateToDate),
    };
    this.workflow
      .queue(q)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (r) => {
          this.rowData = r ?? [];
          this.refreshActionFilterOptions();
          setTimeout(() => this.gridApi?.refreshCells({ force: true }), 0);
        },
        error: () => {
          this.rowData = [];
          this.refreshActionFilterOptions();
        },
      });
  }

  openPayload(row: WorkflowInstanceRow | undefined): void {
    if (!row) return;
    this.dialog.open(WorkflowPayloadDialogComponent, {
      width: '560px',
      data: { json: row.payloadJson ?? '{}' },
    });
  }

  openReject(row: WorkflowInstanceRow): void {
    this.dialog
      .open(WorkflowRejectDialogComponent, {
        width: '440px',
        data: {},
        disableClose: true,
      })
      .afterClosed()
      .subscribe((comment) => {
        if (comment === undefined) {
          return;
        }
        this.act(row.id, () => this.workflow.reject(row.id, comment));
      });
  }

  approve(id: number): void {
    this.act(id, () => this.workflow.approve(id));
  }

  private act(id: number, op: () => ReturnType<UmWorkflowService['approve']>): void {
    if (this.actingId != null) return;
    this.actingId = id;
    op()
      .pipe(finalize(() => (this.actingId = null)))
      .subscribe({ next: () => this.load(), error: () => {} });
  }
}
