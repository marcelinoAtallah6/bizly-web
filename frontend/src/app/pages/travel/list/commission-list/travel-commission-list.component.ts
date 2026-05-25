import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ColDef, GridApi, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { TravelCommissionRuleRow } from 'src/app/core/models/travel.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import {
  TravelCommissionFormDialogComponent,
  TravelCommissionFormDialogData,
} from '../../dialogs/commission-form-dialog/travel-commission-form-dialog.component';
import { TravelKpiItem } from '../../shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelCommissionService } from '../../services/travel-commission.service';

@Component({
  selector: 'app-travel-commission-list',
  templateUrl: './travel-commission-list.component.html',
})
export class TravelCommissionListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: TravelCommissionRuleRow[] = [];
  loading = false;
  readonly route = '/travel/commissions';
  kpis: TravelKpiItem[] = [];
  private gridApi: GridApi<TravelCommissionRuleRow> | null = null;
  selected: TravelCommissionRuleRow | null = null;

  columnDefs: ColDef[] = [
    { field: 'name', headerName: 'Rule', flex: 1.2, sortable: true, filter: true },
    { field: 'ruleType', headerName: 'Type', width: 120, filter: true },
    { field: 'ratePercent', headerName: 'Rate %', width: 100 },
    { field: 'flatAmount', headerName: 'Flat', width: 100 },
    {
      field: 'active',
      headerName: 'Active',
      width: 90,
      valueFormatter: (p) => (p.value === true ? 'Yes' : 'No'),
    },
  ];

  defaultColDef: ColDef = { resizable: true, floatingFilter: true };

  constructor(
    private readonly api: TravelCommissionService,
    private readonly dialog: MatDialog,
    private readonly menuPerm: MenuPermissionService
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
      {
        id: 'add',
        icon: 'add_box',
        tooltip: 'New commission rule',
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
            { label: 'Rules', value: this.rowData.length, icon: 'percent', tone: 'finance' },
            { label: 'Active', value: this.rowData.filter((r) => r.active !== false).length, icon: 'check_circle', tone: 'packages' },
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

  onRowDoubleClicked(e: RowDoubleClickedEvent<TravelCommissionRuleRow>): void {
    if (e.data) {
      this.openDialog(e.data);
    }
  }

  openDialog(row?: TravelCommissionRuleRow): void {
    const ref = this.dialog.open(TravelCommissionFormDialogComponent, {
      width: '520px',
      data: { row } as TravelCommissionFormDialogData,
    });
    ref.afterClosed().subscribe((ok) => {
      if (ok) {
        this.load();
      }
    });
  }

  removeSelected(): void {
    if (!this.selected?.id || !confirm(`Delete rule "${this.selected.name}"?`)) {
      return;
    }
    this.api.delete({ id: this.selected.id }).subscribe({ next: () => this.load() });
  }
}
