import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ColDef, GridApi, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { TravelSupplierRow } from 'src/app/core/models/travel.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import {
  TravelSupplierFormDialogComponent,
  TravelSupplierFormDialogData,
} from '../../dialogs/supplier-form-dialog/travel-supplier-form-dialog.component';
import { TravelKpiItem } from '../../shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelSupplierService } from '../../services/travel-supplier.service';

@Component({
  selector: 'app-travel-supplier-list',
  templateUrl: './travel-supplier-list.component.html',
})
export class TravelSupplierListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: TravelSupplierRow[] = [];
  loading = false;
  readonly route = '/travel/suppliers';
  kpis: TravelKpiItem[] = [];
  private gridApi: GridApi<TravelSupplierRow> | null = null;
  selected: TravelSupplierRow | null = null;

  columnDefs: ColDef[] = [
    { field: 'name', headerName: 'Supplier', flex: 1.2, sortable: true, filter: true },
    { field: 'supplierType', headerName: 'Type', width: 120, filter: true },
    { field: 'contactEmail', headerName: 'Email', flex: 1 },
    { field: 'contactPhone', headerName: 'Phone', width: 140 },
    {
      field: 'active',
      headerName: 'Active',
      width: 90,
      valueFormatter: (p) => (p.value === true ? 'Yes' : 'No'),
    },
  ];

  defaultColDef: ColDef = { resizable: true, floatingFilter: true };

  constructor(
    private readonly api: TravelSupplierService,
    private readonly dialog: MatDialog,
    private readonly menuPerm: MenuPermissionService
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
      {
        id: 'add',
        icon: 'add_box',
        tooltip: 'New supplier',
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
          const tones: TravelKpiItem['tone'][] = ['clients', 'packages', 'bookings', 'open', 'finance'];
          const typeCounts = new Map<string, number>();
          for (const s of this.rowData) {
            const t = (s.supplierType ?? 'OTHER').toUpperCase();
            typeCounts.set(t, (typeCounts.get(t) ?? 0) + 1);
          }
          this.kpis = [
            { label: 'Total suppliers', value: this.rowData.length, icon: 'handshake', tone: 'clients' },
            ...Array.from(typeCounts.entries()).map(([type, count], i) => ({
              label: type,
              value: count,
              icon: this.iconForSupplierType(type),
              tone: tones[(i + 1) % tones.length],
            })),
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

  onRowDoubleClicked(e: RowDoubleClickedEvent<TravelSupplierRow>): void {
    if (e.data) {
      this.openDialog(e.data);
    }
  }

  openDialog(row?: TravelSupplierRow): void {
    const ref = this.dialog.open(TravelSupplierFormDialogComponent, {
      width: '520px',
      data: { row } as TravelSupplierFormDialogData,
    });
    ref.afterClosed().subscribe((ok) => {
      if (ok) {
        this.load();
      }
    });
  }

  iconForSupplierType(type: string): string {
    switch (type) {
      case 'AIRLINE':
        return 'flight';
      case 'HOTEL':
        return 'hotel';
      case 'DMC':
        return 'map';
      case 'TRANSPORT':
        return 'directions_bus';
      case 'INSURANCE':
        return 'health_and_safety';
      default:
        return 'store';
    }
  }

  removeSelected(): void {
    if (!this.selected?.id || !confirm(`Delete supplier "${this.selected.name}"?`)) {
      return;
    }
    this.api.delete({ id: this.selected.id }).subscribe({ next: () => this.load() });
  }
}
