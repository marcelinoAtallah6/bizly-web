import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ColDef, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { SaleSummaryResponse } from 'src/app/core/models/pm.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import {
  SaleDetailIconGridContext,
  saleDetailsIconCellRenderer,
} from 'src/app/shared/ag-grid/renderers/sale-details-icon.renderer';
import { PmSaleService } from '../../pm/services/pm-sale.service';
import { SaleDetailDialogComponent } from 'src/app/shared/sale-detail-dialog/sale-detail-dialog.component';

@Component({
  selector: 'app-payment-history',
  templateUrl: './payment-history.component.html',
  styleUrl: './payment-history.component.scss',
})
export class PaymentHistoryComponent implements OnInit {
  loading = true;
  rowData: SaleSummaryResponse[] = [];
  columnDefs: ColDef<SaleSummaryResponse>[] = [];
  defaultColDef: ColDef = {
    resizable: true,
    sortable: true,
    floatingFilter: true,
    flex: 1,
  };

  /** Passed to ag-grid so the view icon calls the same handler as {@link onRowDoubleClicked}. */
  gridContext!: SaleDetailIconGridContext;

  constructor(
    private readonly pmSale: PmSaleService,
    private readonly dialog: MatDialog,
    private readonly snack: MatSnackBar
  ) {}

  get pageToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
    ];
  }

  ngOnInit(): void {
    this.gridContext = {
      openSaleDetail: (id: number) => this.openSaleDetail(id),
    };

    this.columnDefs = [
      { field: 'id', headerName: 'Sale #', maxWidth: 120 },
      { field: 'customerId', headerName: 'Customer ID', maxWidth: 130 },
      {
        field: 'customerDisplayName',
        headerName: 'Customer name',
        minWidth: 180,
        valueFormatter: (p) => p.value ?? '—',
      },
      {
        field: 'totalAmount',
        headerName: 'Total',
        maxWidth: 130,
        valueFormatter: (p) => (p.value != null ? Number(p.value).toFixed(2) : ''),
      },
      { field: 'status', headerName: 'Status', maxWidth: 120 },
      { field: 'createdAt', headerName: 'Date', minWidth: 160 },
      {
        colId: 'actions',
        headerName: '',
        maxWidth: 72,
        minWidth: 72,
        sortable: false,
        filter: false,
        suppressHeaderMenuButton: true,
        cellRenderer: saleDetailsIconCellRenderer,
      },
    ];
    this.load();
  }

  load(): void {
    this.loading = true;
    this.pmSale
      .gets({ pageNumber: 0, pageSize: 200 })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (page) => {
          this.rowData = page.items ?? [];
        },
        error: () => {},
      });
  }

  onGridReady(_e: GridReadyEvent): void {}

  onRowDoubleClicked(e: RowDoubleClickedEvent<SaleSummaryResponse>): void {
    const id = e.data?.id;
    if (id != null) {
      this.openSaleDetail(typeof id === 'number' ? id : Number(id));
    }
  }

  /** Same dialog + payload as row double-click; used from grid context for the view icon. */
  openSaleDetail(id: number): void {
    if (!Number.isFinite(id)) {
      return;
    }
    this.pmSale.get({ id }).subscribe({
      next: (sale) => {
        this.dialog.open(SaleDetailDialogComponent, {
          width: '580px',
          maxWidth: '95vw',
          data: sale,
          panelClass: 'sale-detail-dialog-panel',
          autoFocus: 'first-tabbable',
        });
      },
      error: () => {
        this.snack.open('Could not load sale details.', 'Dismiss', { duration: 5000 });
      },
    });
  }
}
