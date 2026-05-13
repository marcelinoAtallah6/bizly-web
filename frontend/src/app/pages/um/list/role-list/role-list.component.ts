import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ColDef, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { GetRoleResponse } from 'src/app/core/models/um.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { UmRoleService } from '../../services/um-role.service';

@Component({
  selector: 'app-role-list',
  templateUrl: './role-list.component.html',
  styleUrl: './role-list.component.scss',
})
export class RoleListComponent implements OnInit {
  rowData: GetRoleResponse[] = [];
  loading = false;

  columnDefs: ColDef[] = [
    { field: 'id', headerName: 'ID', width: 90, sortable: true, filter: true },
    { field: 'name', headerName: 'Name', flex: 1, sortable: true, filter: true },
    { field: 'roleType', headerName: 'Role type', width: 130, sortable: true, filter: true },
    { field: 'createdAt', headerName: 'Created', flex: 1, sortable: true, filter: true },
  ];

  defaultColDef: ColDef = {
    resizable: true,
    floatingFilter: true,
  };

  constructor(
    private readonly umRoleService: UmRoleService,
    private readonly router: Router,
    private readonly menuPerm: MenuPermissionService
  ) {}

  get listToolbar(): ToolbarButton[] {
    const out: ToolbarButton[] = [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
    ];
    if (this.menuPerm.can('/um/role', 'add')) {
      out.push({
        id: 'add',
        icon: 'badge',
        tooltip: 'New role',
        action: () => this.goNew(),
        color: 'primary',
      });
    }
    return out;
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.umRoleService
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

  onRowDoubleClicked(event: RowDoubleClickedEvent<GetRoleResponse>): void {
    const id = event.data?.id;
    if (id != null) {
      this.router.navigate(['/um', 'role', id]);
    }
  }

  goNew(): void {
    this.router.navigate(['/um', 'role', 'new']);
  }
}
