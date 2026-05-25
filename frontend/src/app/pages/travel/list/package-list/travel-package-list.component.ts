import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ColDef, GridApi, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { TravelPackageRow } from 'src/app/core/models/travel.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import {
  TravelPackageFormDialogComponent,
  TravelPackageFormDialogData,
} from '../../dialogs/package-form-dialog/travel-package-form-dialog.component';
import { TravelKpiItem } from '../../shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelPackageService } from '../../services/travel-package.service';

@Component({
  selector: 'app-travel-package-list',
  templateUrl: './travel-package-list.component.html',
})
export class TravelPackageListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: TravelPackageRow[] = [];
  loading = false;
  readonly route = '/travel/packages';
  kpis: TravelKpiItem[] = [];
  private gridApi: GridApi<TravelPackageRow> | null = null;
  selected: TravelPackageRow | null = null;

  columnDefs: ColDef[] = [
    { field: 'code', headerName: 'Code', width: 100, sortable: true, filter: true },
    { field: 'name', headerName: 'Package', flex: 1.2, sortable: true, filter: true },
    { field: 'destination', headerName: 'Destination', flex: 1, sortable: true, filter: true },
    { field: 'durationDays', headerName: 'Days', width: 90 },
    {
      field: 'basePrice',
      headerName: 'Price',
      width: 120,
      valueFormatter: (p) => (p.value != null ? Number(p.value).toFixed(2) : ''),
    },
    { field: 'currency', headerName: 'Cur', width: 70 },
    {
      field: 'active',
      headerName: 'Active',
      width: 90,
      valueFormatter: (p) => (p.value === true ? 'Yes' : 'No'),
    },
  ];

  defaultColDef: ColDef = { resizable: true, floatingFilter: true };

  constructor(
    private readonly api: TravelPackageService,
    private readonly dialog: MatDialog,
    private readonly menuPerm: MenuPermissionService
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
      {
        id: 'add',
        icon: 'add_box',
        tooltip: 'New package',
        action: () => this.openDialog(),
        color: 'primary',
        hidden: !this.menuPerm.can(this.route, 'add'),
      },
      {
        id: 'edit',
        icon: 'edit',
        tooltip: 'Edit selected',
        action: () => this.openDialog(this.selected ?? undefined),
        hidden: !this.menuPerm.can(this.route, 'edit'),
        disabled: !this.selected,
      },
      {
        id: 'delete',
        icon: 'delete',
        tooltip: 'Delete selected',
        action: () => this.removeSelected(),
        hidden: !this.menuPerm.can(this.route, 'delete'),
        disabled: !this.selected,
      },
    ];
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.api
      .gets({ pageNumber: 0, pageSize: 200 })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (p) => {
          this.rowData = p.items ?? [];
          this.kpis = [
            { label: 'Packages', value: this.rowData.length, icon: 'luggage', tone: 'packages' },
            { label: 'Active', value: this.rowData.filter((x) => x.active !== false).length, icon: 'check_circle', tone: 'clients' },
            {
              label: 'With capacity',
              value: this.rowData.filter((x) => x.maxCapacityPerDay != null && x.maxCapacityPerDay > 0).length,
              icon: 'event_seat',
              tone: 'bookings',
            },
          ];
        },
      });
  }

  onGridReady(e: GridReadyEvent): void {
    this.gridApi = e.api;
  }

  onSelectionChanged(): void {
    const rows = this.gridApi?.getSelectedRows() ?? [];
    this.selected = rows[0] ?? null;
  }

  onRowDoubleClicked(e: RowDoubleClickedEvent<TravelPackageRow>): void {
    if (e.data) {
      this.openDialog(e.data);
    }
  }

  openDialog(row?: TravelPackageRow): void {
    const ref = this.dialog.open(TravelPackageFormDialogComponent, {
      width: '920px',
      maxWidth: '96vw',
      height: 'auto',
      maxHeight: '90vh',
      panelClass: 'travel-package-dialog-pane',
      autoFocus: 'first-tabbable',
      data: { row } as TravelPackageFormDialogData,
    });
    ref.afterClosed().subscribe((ok) => {
      if (ok) {
        this.load();
      }
    });
  }

  removeSelected(): void {
    if (!this.selected?.id || !confirm(`Delete package "${this.selected.name}"?`)) {
      return;
    }
    this.api.delete({ id: this.selected.id }).subscribe({ next: () => this.load() });
  }
}
