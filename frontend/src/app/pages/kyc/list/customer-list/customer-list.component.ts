import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ColDef, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { GetCustomerResponse } from 'src/app/core/models/kyc.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import { KycCustomerService } from '../../services/kyc-customer.service';

@Component({
  selector: 'app-customer-list',
  templateUrl: './customer-list.component.html',
  styleUrl: './customer-list.component.scss',
})
export class CustomerListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: GetCustomerResponse[] = [];
  loading = false;

  columnDefs: ColDef[] = [
    { field: 'id', headerName: 'ID', width: 90, sortable: true, filter: true },
    {
      headerName: 'Name',
      flex: 1,
      sortable: true,
      filter: true,
      valueGetter: (p) => {
        const d = p.data;
        if (!d) return '';
        if (d.fullName) return d.fullName;
        return [d.firstName, d.lastName].filter(Boolean).join(' ').trim();
      },
    },
    { field: 'email', headerName: 'Email', flex: 1, sortable: true, filter: true },
    { field: 'mobileNumber', headerName: 'Mobile', width: 140, sortable: true, filter: true },
    { field: 'dob', headerName: 'DOB', width: 130, sortable: true, filter: true },
  ];

  defaultColDef: ColDef = {
    resizable: true,
    floatingFilter: true,
  };

  private readonly routeCustomers = '/kyc/customers';

  constructor(
    private readonly kycCustomerService: KycCustomerService,
    private readonly router: Router,
    private readonly menuPerm: MenuPermissionService
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
      {
        id: 'add',
        icon: 'person_add',
        tooltip: 'New customer',
        action: () => this.goNew(),
        color: 'primary',
        hidden: !this.menuPerm.can(this.routeCustomers, 'add'),
      },
    ];
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.kycCustomerService
      .gets({ pageNumber: 0, pageSize: 100 })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (page) => {
          this.rowData = page.items ?? [];
        },
        error: () => {},
      });
  }

  onGridReady(_e: GridReadyEvent): void {}

  onRowDoubleClicked(event: RowDoubleClickedEvent<GetCustomerResponse>): void {
    const id = event.data?.id;
    if (id != null) {
      this.router.navigate(['/kyc/customers', id]);
    }
  }

  goNew(): void {
    this.router.navigate(['/kyc/customers/new']);
  }
}
