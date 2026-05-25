import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ColDef, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { TravelClientRow } from 'src/app/core/models/travel.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import { TravelKpiItem } from '../../shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelClientService } from '../../services/travel-client.service';

@Component({
  selector: 'app-travel-client-list',
  templateUrl: './travel-client-list.component.html',
})
export class TravelClientListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: TravelClientRow[] = [];
  loading = false;
  readonly route = '/travel/clients';
  kpis: TravelKpiItem[] = [];

  columnDefs: ColDef[] = [
    { field: 'fullName', headerName: 'Client name', flex: 1.2, sortable: true, filter: true },
    { field: 'email', headerName: 'Email', flex: 1, sortable: true, filter: true },
    { field: 'phone', headerName: 'Phone', width: 140, sortable: true, filter: true },
    { field: 'passportNo', headerName: 'Passport', width: 120, sortable: true, filter: true },
    {
      field: 'active',
      headerName: 'Active',
      width: 100,
      valueFormatter: (p) => (p.value === true ? 'Yes' : 'No'),
    },
  ];

  defaultColDef: ColDef = { resizable: true, floatingFilter: true };

  constructor(
    private readonly api: TravelClientService,
    private readonly router: Router,
    private readonly menuPerm: MenuPermissionService
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
      {
        id: 'add',
        icon: 'person_add',
        tooltip: 'New client',
        action: () => this.router.navigate(['/travel/clients/new']),
        color: 'primary',
        hidden: !this.menuPerm.can(this.route, 'add'),
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
            { label: 'Clients', value: this.rowData.length, icon: 'groups', tone: 'clients' },
            { label: 'Active', value: this.rowData.filter((c) => c.active !== false).length, icon: 'check_circle', tone: 'packages' },
            { label: 'With passport', value: this.rowData.filter((c) => !!c.passportNo?.trim()).length, icon: 'badge', tone: 'bookings' },
          ];
        },
      });
  }

  onGridReady(_e: GridReadyEvent): void {}

  onRowDoubleClicked(e: RowDoubleClickedEvent<TravelClientRow>): void {
    const id = e.data?.id;
    if (id != null) {
      this.router.navigate(['/travel/clients', id]);
    }
  }
}
