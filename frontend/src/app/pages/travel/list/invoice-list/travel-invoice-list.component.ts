import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ColDef, GridApi, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { TravelInvoiceRow } from 'src/app/core/models/travel.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import {
  TravelInvoiceFormDialogComponent,
  TravelInvoiceFormDialogData,
} from '../../dialogs/invoice-form-dialog/travel-invoice-form-dialog.component';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { TravelKpiItem } from '../../shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelInvoiceService } from '../../services/travel-invoice.service';

@Component({
  selector: 'app-travel-invoice-list',
  templateUrl: './travel-invoice-list.component.html',
})
export class TravelInvoiceListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: TravelInvoiceRow[] = [];
  loading = false;
  readonly route = '/travel/finance';
  kpis: TravelKpiItem[] = [];
  private gridApi: GridApi<TravelInvoiceRow> | null = null;
  selected: TravelInvoiceRow | null = null;

  columnDefs: ColDef[] = [
    { field: 'invoiceNo', headerName: 'Invoice #', width: 120, sortable: true, filter: true },
    {
      headerName: 'Booking',
      flex: 1,
      valueGetter: (p) => this.lookup.bookingLabel(p.data?.bookingId),
    },
    {
      field: 'amount',
      headerName: 'Amount',
      width: 110,
      valueFormatter: (p) => (p.value != null ? Number(p.value).toFixed(2) : ''),
    },
    { field: 'currency', headerName: 'Cur', width: 70 },
    { field: 'status', headerName: 'Status', width: 110, filter: true },
    { field: 'issuedAt', headerName: 'Issued', width: 140 },
    { field: 'dueAt', headerName: 'Due', width: 140 },
  ];

  defaultColDef: ColDef = { resizable: true, floatingFilter: true };

  constructor(
    private readonly api: TravelInvoiceService,
    private readonly lookup: TravelLookupService,
    private readonly dialog: MatDialog,
    private readonly menuPerm: MenuPermissionService
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
      {
        id: 'add',
        icon: 'add_box',
        tooltip: 'New invoice',
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
    this.lookup.ensureAllForGrids().subscribe({ next: () => this.load(), error: () => this.load() });
  }

  load(): void {
    this.loading = true;
    this.api
      .gets({ pageNumber: 0, pageSize: 200 })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (p) => {
          this.rowData = p.items ?? [];
          const total = this.rowData.reduce((s, i) => s + (Number(i.amount) || 0), 0);
          this.kpis = [
            { label: 'Invoices', value: this.rowData.length, icon: 'receipt_long', tone: 'finance' },
            { label: 'Outstanding', value: this.rowData.filter((i) => i.status !== 'PAID' && i.status !== 'CANCELLED').length, icon: 'schedule', tone: 'open' },
            { label: 'Total billed', value: total.toFixed(2), icon: 'payments', tone: 'bookings' },
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

  onRowDoubleClicked(e: RowDoubleClickedEvent<TravelInvoiceRow>): void {
    if (e.data) {
      this.openDialog(e.data);
    }
  }

  openDialog(row?: TravelInvoiceRow): void {
    const ref = this.dialog.open(TravelInvoiceFormDialogComponent, {
      width: '560px',
      data: { row } as TravelInvoiceFormDialogData,
    });
    ref.afterClosed().subscribe((ok) => {
      if (ok) {
        this.load();
      }
    });
  }

  removeSelected(): void {
    if (!this.selected?.id || !confirm('Delete this invoice?')) {
      return;
    }
    this.api.delete({ id: this.selected.id }).subscribe({ next: () => this.load() });
  }
}
