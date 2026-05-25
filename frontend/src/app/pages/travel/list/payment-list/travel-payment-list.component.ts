import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ColDef, GridApi, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { TravelPaymentRow } from 'src/app/core/models/travel.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import {
  TravelPaymentFormDialogComponent,
  TravelPaymentFormDialogData,
} from '../../dialogs/payment-form-dialog/travel-payment-form-dialog.component';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { TravelPaymentService } from '../../services/travel-payment.service';

@Component({
  selector: 'app-travel-payment-list',
  templateUrl: './travel-payment-list.component.html',
})
export class TravelPaymentListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: TravelPaymentRow[] = [];
  loading = false;
  readonly route = '/travel/finance';
  private gridApi: GridApi<TravelPaymentRow> | null = null;
  selected: TravelPaymentRow | null = null;

  columnDefs: ColDef[] = [
    {
      headerName: 'Invoice',
      flex: 1.2,
      sortable: true,
      filter: true,
      valueGetter: (p) => this.lookup.invoiceLabel(p.data?.invoiceId),
    },
    {
      field: 'amount',
      headerName: 'Amount',
      width: 120,
      valueFormatter: (p) => (p.value != null ? Number(p.value).toFixed(2) : ''),
    },
    { field: 'paymentMethod', headerName: 'Method', width: 130, filter: true },
    { field: 'paidAt', headerName: 'Paid at', width: 150 },
    { field: 'referenceNo', headerName: 'Reference', flex: 1, filter: true },
  ];

  defaultColDef: ColDef = { resizable: true, floatingFilter: true };

  constructor(
    private readonly api: TravelPaymentService,
    private readonly lookup: TravelLookupService,
    private readonly dialog: MatDialog,
    private readonly menuPerm: MenuPermissionService
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
      {
        id: 'add',
        icon: 'receipt_long',
        tooltip: 'Record payment',
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
    this.lookup.ensureCatalog('invoices').subscribe({ next: () => this.load(), error: () => this.load() });
  }

  load(): void {
    this.loading = true;
    this.api
      .gets({ pageNumber: 0, pageSize: 200 })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({ next: (p) => (this.rowData = p.items ?? []) });
  }

  onGridReady(e: GridReadyEvent): void {
    this.gridApi = e.api;
  }

  onSelectionChanged(): void {
    const rows = this.gridApi?.getSelectedRows() ?? [];
    this.selected = rows[0] ?? null;
  }

  onRowDoubleClicked(e: RowDoubleClickedEvent<TravelPaymentRow>): void {
    if (e.data) {
      this.openDialog(e.data);
    }
  }

  openDialog(row?: TravelPaymentRow): void {
    const ref = this.dialog.open(TravelPaymentFormDialogComponent, {
      width: '520px',
      data: { row } as TravelPaymentFormDialogData,
    });
    ref.afterClosed().subscribe((ok) => {
      if (ok) {
        this.load();
      }
    });
  }

  removeSelected(): void {
    if (!this.selected?.id || !confirm('Delete this payment record?')) {
      return;
    }
    this.api.delete({ id: this.selected.id }).subscribe({ next: () => this.load() });
  }
}
