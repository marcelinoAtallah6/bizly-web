import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ColDef, GridApi, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { TravelFollowUpRow } from 'src/app/core/models/travel.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import {
  TravelFollowUpFormDialogComponent,
  TravelFollowUpFormDialogData,
} from '../../dialogs/follow-up-form-dialog/travel-follow-up-form-dialog.component';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { TravelKpiItem } from '../../shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelFollowUpService } from '../../services/travel-follow-up.service';

@Component({
  selector: 'app-travel-follow-up-list',
  templateUrl: './travel-follow-up-list.component.html',
})
export class TravelFollowUpListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: TravelFollowUpRow[] = [];
  loading = false;
  readonly route = '/travel/follow-ups';
  kpis: TravelKpiItem[] = [];
  private gridApi: GridApi<TravelFollowUpRow> | null = null;
  selected: TravelFollowUpRow | null = null;

  columnDefs: ColDef[] = [
    { field: 'subject', headerName: 'Subject', flex: 1.2, sortable: true, filter: true },
    {
      headerName: 'Client',
      width: 140,
      valueGetter: (p) => this.lookup.clientLabel(p.data?.clientId),
    },
    {
      headerName: 'Booking',
      width: 130,
      valueGetter: (p) => this.lookup.bookingLabel(p.data?.bookingId),
    },
    { field: 'status', headerName: 'Status', width: 110, filter: true },
    { field: 'dueAt', headerName: 'Due', width: 150 },
    { field: 'assignedTo', headerName: 'Consultant', width: 140 },
  ];

  defaultColDef: ColDef = { resizable: true, floatingFilter: true };

  constructor(
    private readonly api: TravelFollowUpService,
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
        tooltip: 'New follow-up',
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
            { label: 'Follow-ups', value: this.rowData.length, icon: 'forum', tone: 'open' },
            { label: 'Open', value: this.rowData.filter((f) => f.status === 'OPEN' || f.status === 'PENDING').length, icon: 'pending', tone: 'bookings' },
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

  onRowDoubleClicked(e: RowDoubleClickedEvent<TravelFollowUpRow>): void {
    if (e.data) {
      this.openDialog(e.data);
    }
  }

  openDialog(row?: TravelFollowUpRow): void {
    const ref = this.dialog.open(TravelFollowUpFormDialogComponent, {
      width: '560px',
      data: { row } as TravelFollowUpFormDialogData,
    });
    ref.afterClosed().subscribe((ok) => {
      if (ok) {
        this.load();
      }
    });
  }

  removeSelected(): void {
    if (!this.selected?.id || !confirm('Delete this follow-up?')) {
      return;
    }
    this.api.delete({ id: this.selected.id }).subscribe({ next: () => this.load() });
  }
}
