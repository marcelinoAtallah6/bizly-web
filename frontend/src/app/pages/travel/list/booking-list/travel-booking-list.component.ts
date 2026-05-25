import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ColDef, GridApi, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { TravelBookingRow } from 'src/app/core/models/travel.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import {
  TravelBookingFormDialogComponent,
  TravelBookingFormDialogData,
} from '../../dialogs/booking-form-dialog/travel-booking-form-dialog.component';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { TravelKpiItem } from '../../shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelBookingService } from '../../services/travel-booking.service';

@Component({
  selector: 'app-travel-booking-list',
  templateUrl: './travel-booking-list.component.html',
})
export class TravelBookingListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: TravelBookingRow[] = [];
  loading = false;
  readonly menuRoute = '/travel/bookings';
  kpis: TravelKpiItem[] = [];
  statusFilter: 'ALL' | 'ENQUIRY' | 'QUOTED' | 'CONFIRMED' = 'ALL';
  private allRows: TravelBookingRow[] = [];
  private gridApi: GridApi<TravelBookingRow> | null = null;
  selected: TravelBookingRow | null = null;

  columnDefs: ColDef[] = [
    { field: 'referenceNo', headerName: 'Reference', width: 130, sortable: true, filter: true },
    {
      headerName: 'Client',
      flex: 1.2,
      sortable: true,
      filter: true,
      valueGetter: (p) => this.lookup.clientLabel(p.data?.clientId),
    },
    {
      headerName: 'Package',
      flex: 1,
      valueGetter: (p) => this.lookup.packageLabel(p.data?.packageId),
    },
    { field: 'status', headerName: 'Status', width: 120, sortable: true, filter: true },
    { field: 'timelineStage', headerName: 'Stage', width: 120 },
    { field: 'departureDate', headerName: 'Departure', width: 120 },
    { field: 'returnDate', headerName: 'Return', width: 120 },
    {
      field: 'totalAmount',
      headerName: 'Total',
      width: 110,
      valueFormatter: (p) => (p.value != null ? Number(p.value).toFixed(2) : ''),
    },
    { field: 'currency', headerName: 'Cur', width: 70 },
  ];

  defaultColDef: ColDef = { resizable: true, floatingFilter: true };

  constructor(
    private readonly api: TravelBookingService,
    private readonly lookup: TravelLookupService,
    private readonly dialog: MatDialog,
    private readonly menuPerm: MenuPermissionService,
    private readonly activatedRoute: ActivatedRoute,
    private readonly router: Router
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
      {
        id: 'add',
        icon: 'event_available',
        tooltip: 'New booking',
        action: () => this.openDialog(),
        color: 'primary',
        hidden: !this.menuPerm.can(this.menuRoute, 'add'),
      },
      {
        id: 'edit',
        icon: 'edit',
        tooltip: 'Edit selected',
        action: () => this.openDialog(this.selected ?? undefined),
        hidden: !this.menuPerm.can(this.menuRoute, 'edit'),
        disabled: !this.selected,
      },
      {
        id: 'delete',
        icon: 'delete',
        tooltip: 'Delete selected',
        action: () => this.removeSelected(),
        hidden: !this.menuPerm.can(this.menuRoute, 'delete'),
        disabled: !this.selected,
      },
    ];
  }

  ngOnInit(): void {
    this.lookup.ensureAllForGrids().subscribe({ next: () => this.load(), error: () => this.load() });
  }

  load(): void {
    this.loading = true;
    this.api
      .gets({ pageNumber: 0, pageSize: 200 })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (p) => {
          this.allRows = p.items ?? [];
          this.applyFilter();
          this.openBookingFromQueryParam();
        },
      });
  }

  private openBookingFromQueryParam(): void {
    const raw = this.activatedRoute.snapshot.queryParamMap.get('id');
    const id = raw != null ? Number(raw) : NaN;
    if (!Number.isFinite(id) || id <= 0) {
      return;
    }
    const row = this.allRows.find((b) => b.id === id);
    if (row) {
      this.openDialog(row);
    }
    void this.router.navigate([], {
      relativeTo: this.activatedRoute,
      queryParams: { id: null },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }

  applyFilter(): void {
    this.rowData =
      this.statusFilter === 'ALL'
        ? this.allRows
        : this.allRows.filter((b) => (b.status ?? '').toUpperCase() === this.statusFilter);
    this.kpis = [
      { label: 'All', value: this.allRows.length, icon: 'list', tone: 'clients' },
      { label: 'Enquiries', value: this.allRows.filter((b) => b.status === 'ENQUIRY').length, icon: 'help', tone: 'open' },
      { label: 'Quoted', value: this.allRows.filter((b) => b.status === 'QUOTED').length, icon: 'request_quote', tone: 'bookings' },
      { label: 'Confirmed', value: this.allRows.filter((b) => b.status === 'CONFIRMED').length, icon: 'verified', tone: 'packages' },
    ];
  }

  onGridReady(e: GridReadyEvent): void {
    this.gridApi = e.api;
  }

  onSelectionChanged(): void {
    const rows = this.gridApi?.getSelectedRows() ?? [];
    this.selected = rows[0] ?? null;
  }

  onRowDoubleClicked(e: RowDoubleClickedEvent<TravelBookingRow>): void {
    if (e.data) {
      this.openDialog(e.data);
    }
  }

  openDialog(row?: TravelBookingRow): void {
    const ref = this.dialog.open(TravelBookingFormDialogComponent, {
      width: '920px',
      maxWidth: '96vw',
      maxHeight: '90vh',
      panelClass: 'travel-form-dialog-panel',
      data: { row } as TravelBookingFormDialogData,
    });
    ref.afterClosed().subscribe((ok) => {
      if (ok) {
        this.lookup.invalidate();
        this.lookup.ensureAllForGrids().subscribe({ next: () => this.load() });
      }
    });
  }

  removeSelected(): void {
    if (!this.selected?.id || !confirm('Delete this booking?')) {
      return;
    }
    this.api.delete({ id: this.selected.id }).subscribe({ next: () => this.load() });
  }
}
