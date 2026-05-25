import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ColDef, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { TravelDestinationRow } from 'src/app/core/models/travel.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import {
  TravelDestinationFormDialogComponent,
  TravelDestinationFormDialogData,
} from '../../dialogs/destination-form-dialog/travel-destination-form-dialog.component';
import { TravelKpiItem } from '../../shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelDestinationService } from '../../services/travel-destination.service';

@Component({
  selector: 'app-travel-destination-list',
  templateUrl: './travel-destination-list.component.html',
})
export class TravelDestinationListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: TravelDestinationRow[] = [];
  loading = false;
  readonly route = '/travel/destinations';
  kpis: TravelKpiItem[] = [];
  private selected: TravelDestinationRow | null = null;
  private gridApi: any;

  columnDefs: ColDef[] = [
    { field: 'name', headerName: 'Destination', flex: 1.2, sortable: true, filter: true },
    { field: 'country', headerName: 'Country', width: 120, filter: true },
    { field: 'region', headerName: 'Region', width: 120 },
    { field: 'riskLevel', headerName: 'Risk', width: 100, filter: true },
    {
      field: 'active',
      headerName: 'Active',
      width: 90,
      valueFormatter: (p) => (p.value === true ? 'Yes' : 'No'),
    },
  ];

  defaultColDef: ColDef = { resizable: true, floatingFilter: true };

  constructor(
    private readonly api: TravelDestinationService,
    private readonly dialog: MatDialog,
    private readonly menuPerm: MenuPermissionService
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
      {
        id: 'add',
        icon: 'add_location',
        tooltip: 'New destination',
        action: () => this.openDialog(),
        color: 'primary',
        hidden: !this.menuPerm.can(this.route, 'add'),
      },
      {
        id: 'edit',
        icon: 'edit',
        tooltip: 'Edit',
        action: () => this.openDialog(this.selected ?? undefined),
        hidden: !this.menuPerm.can(this.route, 'edit'),
        disabled: !this.selected,
      },
      {
        id: 'delete',
        icon: 'delete',
        tooltip: 'Delete',
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
          const high = this.rowData.filter((d) => d.riskLevel === 'HIGH' || d.riskLevel === 'EXTREME').length;
          this.kpis = [
            { label: 'Destinations', value: this.rowData.length, icon: 'public', tone: 'clients' },
            { label: 'High risk', value: high, icon: 'warning', tone: 'open' },
            { label: 'Active', value: this.rowData.filter((d) => d.active !== false).length, icon: 'check_circle', tone: 'packages' },
          ];
        },
      });
  }

  onGridReady(e: GridReadyEvent): void {
    this.gridApi = e.api;
  }

  onSelectionChanged(): void {
    this.selected = this.gridApi?.getSelectedRows()?.[0] ?? null;
  }

  onRowDoubleClicked(e: RowDoubleClickedEvent<TravelDestinationRow>): void {
    if (e.data) {
      this.openDialog(e.data);
    }
  }

  openDialog(row?: TravelDestinationRow): void {
    this.dialog
      .open(TravelDestinationFormDialogComponent, {
        width: '720px',
        maxHeight: '90vh',
        data: { row } as TravelDestinationFormDialogData,
      })
      .afterClosed()
      .subscribe((ok) => {
        if (ok) {
          this.load();
        }
      });
  }

  removeSelected(): void {
    if (!this.selected?.id || !confirm(`Delete destination "${this.selected.name}"?`)) {
      return;
    }
    this.api.delete({ id: this.selected.id }).subscribe({ next: () => this.load() });
  }
}
