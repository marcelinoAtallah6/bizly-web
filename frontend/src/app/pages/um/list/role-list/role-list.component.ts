import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import {
  ColDef,
  GridApi,
  GridReadyEvent,
  RowDoubleClickedEvent,
  SelectionChangedEvent,
} from 'ag-grid-community';
import { finalize } from 'rxjs';
import { GetRoleResponse, GetsRolesRequest, TeamRoleResponse } from 'src/app/core/models/um.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { AuthService } from 'src/app/services/auth.service';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import { SimpleConfirmDialogComponent } from 'src/app/shared/dialogs/simple-confirm-dialog.component';
import { UmTeamRoleService } from '../../services/um-team-role.service';
import { UmRoleService } from '../../services/um-role.service';

@Component({
  selector: 'app-role-list',
  templateUrl: './role-list.component.html',
  styleUrl: './role-list.component.scss',
})
export class RoleListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: GetRoleResponse[] = [];
  loading = false;

  private gridApi: GridApi<GetRoleResponse> | null = null;
  selected: GetRoleResponse | null = null;

  get isBusinessTenant(): boolean {
    return this.auth.isBusinessTenant();
  }

  get pageTitle(): string {
    return this.isBusinessTenant ? 'Team roles' : 'Roles';
  }

  columnDefs: ColDef<GetRoleResponse>[] = [
    { field: 'id', headerName: 'ID', width: 90, sortable: true, filter: true },
    { field: 'name', headerName: 'Name', flex: 1, sortable: true, filter: true },
    { field: 'roleLevelCode', headerName: 'Level', width: 110, sortable: true, filter: true },
    { field: 'roleKind', headerName: 'Kind', width: 160, sortable: true, filter: true },
    { field: 'roleType', headerName: 'Type code', width: 110, sortable: true, filter: true },
    { field: 'createdAt', headerName: 'Created', flex: 1, sortable: true, filter: true },
  ];

  defaultColDef: ColDef = {
    resizable: true,
    floatingFilter: true,
  };

  constructor(
    private readonly umRoleService: UmRoleService,
    private readonly umTeamRoleService: UmTeamRoleService,
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly menuPerm: MenuPermissionService,
    private readonly dialog: MatDialog
  ) {}

  get listToolbar(): ToolbarButton[] {
    const out: ToolbarButton[] = [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
    ];
    if (this.menuPerm.can('/um/role', 'add')) {
      out.push({
        id: 'add',
        icon: 'badge',
        tooltip: this.isBusinessTenant ? 'New team role' : 'New role',
        action: () => this.goNew(),
        color: 'primary',
      });
    }
    if (this.isBusinessTenant && this.menuPerm.can('/um/role', 'delete')) {
      out.push({
        id: 'delete',
        icon: 'delete',
        tooltip: 'Delete selected team role',
        action: () => this.deleteSelected(),
        disabled: this.loading || !this.selected,
        color: 'warn',
      });
    }
    return out;
  }

  ngOnInit(): void {
    if (this.isBusinessTenant) {
      this.columnDefs = [
        { field: 'id', headerName: 'ID', width: 90, sortable: true, filter: true },
        { field: 'name', headerName: 'Name', flex: 1, sortable: true, filter: true },
        {
          field: 'parentRoleId',
          headerName: 'Parent role',
          width: 130,
          sortable: true,
          filter: true,
        },
        { field: 'roleKind', headerName: 'Kind', width: 130, sortable: true, filter: true },
      ];
    }
    this.load();
  }

  load(): void {
    this.loading = true;
    if (this.isBusinessTenant) {
      this.umTeamRoleService
        .list()
        .pipe(finalize(() => (this.loading = false)))
        .subscribe({
          next: (items) => {
            this.rowData = (items ?? []).map((r) => this.teamToRow(r));
            this.gridApi?.setGridOption('rowData', this.rowData);
          },
          error: () => {},
        });
      return;
    }
    this.umRoleService
      .gets(this.buildAdminRoleGetsRequest())
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (page) => {
          this.rowData = page.items ?? [];
          this.gridApi?.setGridOption('rowData', this.rowData);
        },
        error: () => {},
      });
  }

  onGridReady(e: GridReadyEvent<GetRoleResponse>): void {
    this.gridApi = e.api;
  }

  private buildAdminRoleGetsRequest(): GetsRolesRequest {
    const adminCtx = this.auth.getAdminBusinessContext();
    const req: GetsRolesRequest = { pageNumber: 0, pageSize: 200 };
    if (adminCtx != null) {
      req.forBusinessId = adminCtx;
    } else {
      req.globalTemplatesOnly = true;
    }
    return req;
  }

  onSelectionChanged(e: SelectionChangedEvent<GetRoleResponse>): void {
    const rows = e.api.getSelectedRows();
    this.selected = rows.length ? rows[0] : null;
  }

  onRowDoubleClicked(event: RowDoubleClickedEvent<GetRoleResponse>): void {
    const id = event.data?.id;
    if (id != null) {
      this.router.navigate(['/um', 'role', id]);
    }
  }

  goNew(): void {
    this.router.navigate(['/um', 'role', 'new']);
  }

  private teamToRow(r: TeamRoleResponse): GetRoleResponse {
    return {
      id: r.id,
      name: r.name,
      parentRoleId: r.parentRoleId,
      roleKind: 'BUSINESS_TEAM',
      businessId: r.businessId,
    };
  }

  private deleteSelected(): void {
    const row = this.selected;
    if (!row?.id) {
      return;
    }
    const ref = this.dialog.open(SimpleConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Delete team role',
        message: `Delete team role "${row.name}"? Users assigned only this role may lose access.`,
        confirmLabel: 'Delete',
      },
    });
    ref.afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.loading = true;
      this.umTeamRoleService
        .delete({ id: row.id })
        .pipe(finalize(() => (this.loading = false)))
        .subscribe({
          next: () => {
            this.selected = null;
            this.gridApi?.deselectAll();
            this.load();
          },
          error: () => {},
        });
    });
  }
}
