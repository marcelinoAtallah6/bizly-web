import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ColDef, GridApi, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { TravelVisaRow } from 'src/app/core/models/travel.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import {
  TravelVisaFormDialogComponent,
  TravelVisaFormDialogData,
} from '../../dialogs/visa-form-dialog/travel-visa-form-dialog.component';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { TravelKpiItem } from '../../shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelVisaService } from '../../services/travel-visa.service';

@Component({
  selector: 'app-travel-visa-list',
  templateUrl: './travel-visa-list.component.html',
})
export class TravelVisaListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: TravelVisaRow[] = [];
  loading = false;
  readonly route = '/travel/visas';
  kpis: TravelKpiItem[] = [];
  private gridApi: GridApi<TravelVisaRow> | null = null;
  selected: TravelVisaRow | null = null;

  columnDefs: ColDef[] = [
    {
      headerName: 'Client',
      flex: 1.2,
      sortable: true,
      filter: true,
      valueGetter: (p) => this.lookup.clientLabel(p.data?.clientId),
    },
    {
      headerName: 'Booking',
      width: 140,
      valueGetter: (p) => this.lookup.bookingLabel(p.data?.bookingId),
    },
    { field: 'country', headerName: 'Country', width: 120, sortable: true, filter: true },
    { field: 'visaType', headerName: 'Type', width: 110 },
    { field: 'status', headerName: 'Status', width: 120, filter: true },
    { field: 'submittedAt', headerName: 'Submitted', width: 150 },
    { field: 'decisionAt', headerName: 'Decision', width: 150 },
  ];

  defaultColDef: ColDef = { resizable: true, floatingFilter: true };

  constructor(
    private readonly api: TravelVisaService,
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
        tooltip: 'New visa record',
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
          this.kpis = [
            { label: 'Applications', value: this.rowData.length, icon: 'card_travel', tone: 'packages' },
            { label: 'Approved', value: this.rowData.filter((v) => v.status === 'APPROVED').length, icon: 'check_circle', tone: 'clients' },
            { label: 'In progress', value: this.rowData.filter((v) => v.status === 'IN_PROGRESS' || v.status === 'SUBMITTED').length, icon: 'hourglass_top', tone: 'open' },
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

  onRowDoubleClicked(e: RowDoubleClickedEvent<TravelVisaRow>): void {
    if (e.data) {
      this.openDialog(e.data);
    }
  }

  openDialog(row?: TravelVisaRow): void {
    const ref = this.dialog.open(TravelVisaFormDialogComponent, {
      width: 'min(960px, 96vw)',
      maxWidth: '96vw',
      maxHeight: '90vh',
      panelClass: 'travel-form-dialog-panel',
      data: { row } as TravelVisaFormDialogData,
    });
    ref.afterClosed().subscribe((ok) => {
      if (ok) {
        this.load();
      }
    });
  }

  removeSelected(): void {
    if (!this.selected?.id || !confirm('Delete this visa record?')) {
      return;
    }
    this.api.delete({ id: this.selected.id }).subscribe({ next: () => this.load() });
  }
}
