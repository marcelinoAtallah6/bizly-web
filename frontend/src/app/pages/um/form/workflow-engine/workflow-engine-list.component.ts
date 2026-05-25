import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ColDef, GridApi, GridReadyEvent, RowClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import {
  UmWorkflowEngineService,
  WorkflowPipelineRow,
} from '../../services/um-workflow-engine.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';

@Component({
  selector: 'app-workflow-engine-list',
  templateUrl: './workflow-engine-list.component.html',
  styleUrls: ['./workflow-engine-list.component.scss'],
})
export class WorkflowEngineListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  loading = true;
  rowData: WorkflowPipelineRow[] = [];
  statusFilter = '';
  private gridApi: GridApi<WorkflowPipelineRow> | null = null;

  columnDefs: ColDef<WorkflowPipelineRow>[] = [
    { field: 'id', headerName: 'ID', width: 80, sortable: true, filter: true },
    { field: 'actionDisplayName', headerName: 'Action', flex: 1, sortable: true, filter: true },
    { field: 'actionCode', headerName: 'Code', width: 200, sortable: true, filter: true },
    { field: 'versionNo', headerName: 'Ver', width: 70, sortable: true },
    {
      field: 'status',
      headerName: 'Status',
      width: 120,
      sortable: true,
      filter: true,
      cellClassRules: {
        'wf-status-published': (p) => p.value === 'PUBLISHED',
        'wf-status-draft': (p) => p.value === 'DRAFT',
        'wf-status-disabled': (p) => p.value === 'DISABLED',
      },
    },
    { field: 'displayName', headerName: 'Name', flex: 1, sortable: true, filter: true },
    { field: 'stepCount', headerName: 'Steps', width: 90, sortable: true },
    { field: 'businessId', headerName: 'Business', width: 100, sortable: true, filter: true },
    { field: 'publishedAt', headerName: 'Published', width: 160, sortable: true },
  ];

  defaultColDef: ColDef = { resizable: true, floatingFilter: true };

  constructor(
    private readonly engine: UmWorkflowEngineService,
    private readonly router: Router,
    private readonly menuPerm: MenuPermissionService,
    private readonly snackBar: MatSnackBar
  ) {}

  get toolbar(): ToolbarButton[] {
    const out: ToolbarButton[] = [
      {
        id: 'refresh',
        icon: 'refresh',
        tooltip: 'Refresh',
        action: () => this.load(),
        disabled: this.loading,
      },
    ];
    if (this.menuPerm.can('/um/workflow-engine', 'edit')) {
      out.push({
        id: 'new-approval',
        icon: 'shield-check',
        tooltip: 'New approval workflow',
        action: () => this.router.navigate(['/um/workflow-engine/new'], { queryParams: { kind: 'approval' } }),
      });
      out.push({
        id: 'new',
        icon: 'add',
        tooltip: 'New pipeline',
        action: () => this.router.navigate(['/um/workflow-engine/new']),
        color: 'primary',
      });
    }
    return out;
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.engine
      .listDefinitions()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (rows) => {
          this.rowData = this.applyFilter(rows ?? []);
          this.gridApi?.setGridOption('rowData', this.rowData);
        },
        error: (err) => this.snackBar.open(err?.message ?? 'Failed to load pipelines', 'Close', { duration: 4000 }),
      });
  }

  onStatusFilterChange(): void {
    this.engine.listDefinitions().subscribe({
      next: (rows) => {
        this.rowData = this.applyFilter(rows ?? []);
        this.gridApi?.setGridOption('rowData', this.rowData);
      },
    });
  }

  private applyFilter(rows: WorkflowPipelineRow[]): WorkflowPipelineRow[] {
    if (!this.statusFilter) {
      return rows;
    }
    return rows.filter((r) => r.status === this.statusFilter);
  }

  onGridReady(e: GridReadyEvent<WorkflowPipelineRow>): void {
    this.gridApi = e.api;
  }

  onRowClicked(e: RowClickedEvent<WorkflowPipelineRow>): void {
    const row = e.data;
    if (!row?.id) {
      return;
    }
    this.router.navigate(['/um/workflow-engine', row.id, 'edit']);
  }
}
