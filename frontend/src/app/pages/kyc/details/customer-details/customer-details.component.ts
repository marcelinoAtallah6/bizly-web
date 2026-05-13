import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { ColDef, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { GetCustomerResponse } from 'src/app/core/models/kyc.models';
import { SaleSummaryResponse } from 'src/app/core/models/pm.models';
import { PmSaleService } from 'src/app/pages/pm/services/pm-sale.service';
import {
  SaleDetailIconGridContext,
  saleDetailsIconCellRenderer,
} from 'src/app/shared/ag-grid/renderers/sale-details-icon.renderer';
import { SaleDetailDialogComponent } from 'src/app/shared/sale-detail-dialog/sale-detail-dialog.component';
import { SimpleConfirmDialogComponent } from 'src/app/shared/dialogs/simple-confirm-dialog.component';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { ButtonConfig } from 'src/app/pages/ui-components/switch/switch.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { KycCustomerService } from '../../services/kyc-customer.service';

@Component({
  selector: 'app-customer-details',
  templateUrl: './customer-details.component.html',
  styleUrl: './customer-details.component.scss',
})
export class CustomerDetailsComponent implements OnInit {
  customer: GetCustomerResponse | null = null;
  loading = false;
  deleting = false;

  readonly detailTabs: ButtonConfig[] = [
    { value: 'profile', label: 'Profile', icon: 'user' },
    { value: 'purchases', label: 'Purchases', icon: 'shopping_cart' },
  ];
  activeTab: 'profile' | 'purchases' = 'profile';

  private readonly routeCustomers = '/kyc/customers';

  get detailToolbar(): ToolbarButton[] {
    return [
      { id: 'back', icon: 'arrow_back', tooltip: 'Back to list', action: () => this.back() },
      {
        id: 'delete',
        icon: 'delete_outline',
        tooltip: 'Delete customer',
        action: () => this.delete(),
        disabled: !this.customer || this.deleting,
        color: 'warn',
        hidden: !this.menuPerm.can(this.routeCustomers, 'delete'),
      },
      {
        id: 'edit',
        icon: 'edit',
        tooltip: 'Edit customer',
        action: () => this.edit(),
        disabled: !this.customer,
        color: 'primary',
        hidden: !this.menuPerm.can(this.routeCustomers, 'edit'),
      },
    ];
  }

  setDetailTab(value: string): void {
    this.activeTab = value === 'purchases' ? 'purchases' : 'profile';
  }

  saleRows: SaleSummaryResponse[] = [];
  saleColumnDefs: ColDef<SaleSummaryResponse>[] = [];
  saleDefaultColDef: ColDef = {
    resizable: true,
    sortable: true,
    floatingFilter: true,
    flex: 1,
  };

  /** Aligns view icon with {@link onSaleRowDoubleClicked} / {@link openSale}. */
  saleGridContext!: SaleDetailIconGridContext;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly kycCustomerService: KycCustomerService,
    private readonly pmSale: PmSaleService,
    private readonly dialog: MatDialog,
    private readonly snack: MatSnackBar,
    private readonly menuPerm: MenuPermissionService
  ) {}

  ngOnInit(): void {
    this.saleGridContext = {
      openSaleDetail: (saleId: number) => this.openSale(saleId),
    };

    this.saleColumnDefs = [
      { field: 'id', headerName: 'Sale #', maxWidth: 110 },
      {
        field: 'totalAmount',
        headerName: 'Total',
        maxWidth: 120,
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

    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id)) {
      this.router.navigate(['/kyc/customers']);
      return;
    }
    this.load(id);
  }

  load(id: number): void {
    this.loading = true;
    forkJoin({
      customer: this.kycCustomerService.get({ id }),
      sales: this.pmSale.gets({ customerId: id, pageNumber: 0, pageSize: 200 }),
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: ({ customer, sales }) => {
          this.customer = customer;
          this.saleRows = sales.items ?? [];
        },
        error: () => {},
      });
  }

  onSaleGridReady(_e: GridReadyEvent): void {}

  onSaleRowDoubleClicked(e: RowDoubleClickedEvent<SaleSummaryResponse>): void {
    const id = e.data?.id;
    if (id != null) this.openSale(id);
  }

  openSale(id: number): void {
    this.pmSale.get({ id }).subscribe({
      next: (sale) => {
        this.dialog.open(SaleDetailDialogComponent, {
          width: '580px',
          maxWidth: '95vw',
          data: sale,
          panelClass: 'sale-detail-dialog-panel',
        });
      },
      error: () => {
        this.snack.open('Could not load sale details.', 'Dismiss', { duration: 5000 });
      },
    });
  }

  displayName(c: GetCustomerResponse): string {
    if (c.fullName) return c.fullName;
    return [c.firstName, c.lastName].filter(Boolean).join(' ').trim() || '—';
  }

  edit(): void {
    const id = this.customer?.id;
    if (id != null) {
      this.router.navigate(['/kyc/customers', id, 'edit']);
    }
  }

  back(): void {
    this.router.navigate(['/kyc/customers']);
  }

  delete(): void {
    const id = this.customer?.id;
    if (id == null) return;

    const ref = this.dialog.open(SimpleConfirmDialogComponent, {
      data: { title: 'Delete customer', message: 'This cannot be undone.', confirmLabel: 'Delete' },
      width: '360px',
    });
    ref.afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.deleting = true;
      this.kycCustomerService
        .delete({ id })
        .pipe(finalize(() => (this.deleting = false)))
        .subscribe({
          next: () => this.router.navigate(['/kyc/customers']),
          error: () => {},
        });
    });
  }
}
