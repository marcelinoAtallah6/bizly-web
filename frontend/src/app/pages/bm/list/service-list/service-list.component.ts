import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ColDef, GridApi, GridReadyEvent, RowDoubleClickedEvent, SelectionChangedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { GetServiceItemResponse } from 'src/app/core/models/bm.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import {
  ServiceFormDialogComponent,
  ServiceFormDialogData,
} from '../../dialogs/service-form-dialog/service-form-dialog.component';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import { BmServiceItemService } from '../../services/bm-service-item.service';

@Component({
  selector: 'app-service-list',
  templateUrl: './service-list.component.html',
  styleUrl: './service-list.component.scss',
})
export class ServiceListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: GetServiceItemResponse[] = [];
  loading = false;
  includeInactive = false;

  private gridApi: GridApi<GetServiceItemResponse> | null = null;
  selected: GetServiceItemResponse | null = null;

  columnDefs: ColDef<GetServiceItemResponse>[] = [
    { field: 'id', headerName: 'ID', width: 88, sortable: true, filter: true },
    { field: 'name', headerName: 'Name', flex: 1.2, sortable: true, filter: true },
    {
      field: 'price',
      headerName: 'Price',
      width: 120,
      sortable: true,
      filter: true,
      valueFormatter: (p) => (p.value != null ? Number(p.value).toFixed(2) : ''),
    },
    {
      field: 'durationMinutes',
      headerName: 'Min',
      width: 90,
      sortable: true,
      filter: true,
    },
    {
      field: 'active',
      headerName: 'Active',
      width: 100,
      sortable: true,
      filter: true,
      valueFormatter: (p) => (p.value === true ? 'Yes' : 'No'),
    },
  ];

  defaultColDef: ColDef = {
    resizable: true,
    floatingFilter: true,
  };

  readonly routeServices = '/bm/services';

  constructor(
    private readonly api: BmServiceItemService,
    private readonly dialog: MatDialog,
    readonly menuPerm: MenuPermissionService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  get listToolbar(): ToolbarButton[] {
    const canAdd = this.menuPerm.can(this.routeServices, 'add');
    const canEdit = this.menuPerm.can(this.routeServices, 'edit');
    const canDelete = this.menuPerm.can(this.routeServices, 'delete');
    return [
      {
        id: 'refresh',
        icon: 'refresh',
        tooltip: 'Refresh',
        action: () => this.load(),
        disabled: this.loading,
      },
      {
        id: 'add',
        icon: 'add_box',
        tooltip: 'New service',
        action: () => this.openCreate(),
        color: 'primary',
        hidden: !canAdd,
        disabled: this.loading,
      },
      {
        id: 'edit',
        icon: 'edit',
        tooltip: 'Edit selected',
        action: () => this.openEdit(),
        hidden: !canEdit,
        disabled: this.loading || !this.selected,
      },
      {
        id: 'deactivate',
        icon: 'block',
        tooltip: 'Deactivate selected',
        action: () => this.deactivateSelected(),
        hidden: !canDelete,
        disabled: this.loading || !this.selected,
      },
    ];
  }

  onGridReady(e: GridReadyEvent<GetServiceItemResponse>): void {
    this.gridApi = e.api;
  }

  onSelectionChanged(e: SelectionChangedEvent<GetServiceItemResponse>): void {
    const rows = e.api.getSelectedRows();
    this.selected = rows.length ? rows[0] : null;
  }

  onRowDoubleClicked(e: RowDoubleClickedEvent<GetServiceItemResponse>): void {
    const row = e.data;
    if (!row || !this.menuPerm.can(this.routeServices, 'edit')) {
      return;
    }
    this.selected = row;
    this.openEdit();
  }

  load(): void {
    this.loading = true;
    this.api
      .gets({
        pageNumber: 0,
        pageSize: 500,
        includeInactive: this.includeInactive,
      })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (page) => {
          this.rowData = page.items ?? [];
          this.selected = null;
          this.gridApi?.deselectAll();
        },
        error: () => {},
      });
  }

  onIncludeInactiveChange(checked: boolean): void {
    this.includeInactive = checked;
    this.load();
  }

  openCreate(): void {
    const ref = this.dialog.open<ServiceFormDialogComponent, ServiceFormDialogData, boolean>(
      ServiceFormDialogComponent,
      {
        width: '560px',
        maxWidth: '95vw',
        data: { mode: 'create' },
      }
    );
    ref.afterClosed().subscribe((ok) => {
      if (ok) {
        this.load();
      }
    });
  }

  openEdit(): void {
    if (!this.selected) {
      return;
    }
    const ref = this.dialog.open<ServiceFormDialogComponent, ServiceFormDialogData, boolean>(
      ServiceFormDialogComponent,
      {
        width: '560px',
        maxWidth: '95vw',
        data: { mode: 'edit', row: this.selected },
      }
    );
    ref.afterClosed().subscribe((ok) => {
      if (ok) {
        this.load();
      }
    });
  }

  deactivateSelected(): void {
    const row = this.selected;
    if (!row?.id) {
      return;
    }
    if (!confirm(`Deactivate service "${row.name}"? It will no longer be bookable.`)) {
      return;
    }
    this.loading = true;
    this.api
      .deactivate({ id: row.id })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: () => this.load(),
        error: () => {},
      });
  }
}
