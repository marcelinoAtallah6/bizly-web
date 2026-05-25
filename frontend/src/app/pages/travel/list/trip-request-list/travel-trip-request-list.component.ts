import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ColDef, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { TravelTripRequestRow } from 'src/app/core/models/travel.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import {
  TravelTripRequestFormDialogComponent,
  TravelTripRequestFormDialogData,
} from '../../dialogs/trip-request-form-dialog/travel-trip-request-form-dialog.component';
import { TravelKpiItem } from '../../shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { TravelTripRequestService } from '../../services/travel-trip-request.service';

@Component({
  selector: 'app-travel-trip-request-list',
  templateUrl: './travel-trip-request-list.component.html',
})
export class TravelTripRequestListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: TravelTripRequestRow[] = [];
  loading = false;
  readonly route = '/travel/trip-requests';
  kpis: TravelKpiItem[] = [];
  private selected: TravelTripRequestRow | null = null;
  private gridApi: any;

  columnDefs: ColDef[] = [
    {
      field: 'clientId',
      headerName: 'Client',
      flex: 1,
      valueGetter: (p) => this.lookup.clientLabel(p.data?.clientId),
    },
    { field: 'destinationName', headerName: 'Destination', flex: 1, filter: true },
    { field: 'departureDate', headerName: 'Departure', width: 120 },
    { field: 'budgetAmount', headerName: 'Budget', width: 110 },
    { field: 'quotedAmount', headerName: 'Quoted', width: 110 },
    { field: 'status', headerName: 'Status', width: 120, filter: true },
  ];

  defaultColDef: ColDef = { resizable: true, floatingFilter: true };

  constructor(
    private readonly api: TravelTripRequestService,
    private readonly lookup: TravelLookupService,
    private readonly dialog: MatDialog,
    private readonly menuPerm: MenuPermissionService
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
      {
        id: 'add',
        icon: 'post_add',
        tooltip: 'New trip request',
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
    this.lookup.ensureCatalog('clients').subscribe({ next: () => this.load() });
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
            { label: 'Open requests', value: this.rowData.filter((r) => r.status === 'PENDING').length, icon: 'inbox', tone: 'open' },
            { label: 'Quoted', value: this.rowData.filter((r) => r.status === 'QUOTED').length, icon: 'request_quote', tone: 'bookings' },
            { label: 'Converted', value: this.rowData.filter((r) => r.status === 'CONVERTED').length, icon: 'flight_takeoff', tone: 'packages' },
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

  onRowDoubleClicked(e: RowDoubleClickedEvent<TravelTripRequestRow>): void {
    if (e.data) {
      this.openDialog(e.data);
    }
  }

  openDialog(row?: TravelTripRequestRow): void {
    this.dialog
      .open(TravelTripRequestFormDialogComponent, {
        width: '720px',
        maxHeight: '90vh',
        data: { row } as TravelTripRequestFormDialogData,
      })
      .afterClosed()
      .subscribe((ok) => {
        if (ok) {
          this.load();
        }
      });
  }

  removeSelected(): void {
    if (!this.selected?.id || !confirm('Delete this trip request?')) {
      return;
    }
    this.api.delete({ id: this.selected.id }).subscribe({ next: () => this.load() });
  }
}
