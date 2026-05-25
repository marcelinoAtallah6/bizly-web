import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ColDef, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { GetUserResponse } from 'src/app/core/models/um.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { AuthService } from 'src/app/services/auth.service';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { gridListPaginationMixin } from 'src/app/shared/ag-grid/ag-grid-list-pagination.mixin';
import { UmUserService } from '../../services/um-user.service';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.scss',
})
export class UserListComponent implements OnInit {
  readonly gridPagination = gridListPaginationMixin;
  rowData: GetUserResponse[] = [];
  loading = false;

  columnDefs: ColDef<GetUserResponse>[] = [];

  get isSystemAdmin(): boolean {
    return this.auth.isSystemAdmin();
  }

  defaultColDef: ColDef = {
    resizable: true,
    floatingFilter: true,
  };

  constructor(
    private readonly umUserService: UmUserService,
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly menuPerm: MenuPermissionService
  ) {}

  get listToolbar(): ToolbarButton[] {
    const out: ToolbarButton[] = [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
    ];
    if (this.menuPerm.can('/um/user', 'add')) {
      out.push({
        id: 'add',
        icon: 'person_add',
        tooltip: 'New user',
        action: () => this.goNew(),
        color: 'primary',
      });
    }
    return out;
  }

  ngOnInit(): void {
    this.columnDefs = this.buildColumnDefs();
    this.load();
  }

  private buildColumnDefs(): ColDef<GetUserResponse>[] {
    const cols: ColDef<GetUserResponse>[] = [
      { field: 'id', headerName: 'ID', width: 90, sortable: true, filter: true },
      { field: 'username', headerName: 'Username', flex: 1, sortable: true, filter: true },
      {
        headerName: 'Name',
        flex: 1,
        sortable: true,
        filter: true,
        valueGetter: (p) => {
          const d = p.data;
          if (!d) return '';
          return [d.firstName, d.lastName].filter(Boolean).join(' ').trim();
        },
      },
    ];
    if (this.isSystemAdmin) {
      cols.push({
        field: 'businessName',
        headerName: 'Business',
        flex: 1,
        sortable: true,
        filter: true,
        valueGetter: (p) => p.data?.businessName?.trim() || '—',
      });
    }
    cols.push(
      { field: 'email', headerName: 'Email', flex: 1, sortable: true, filter: true },
      { field: 'mobileNumber', headerName: 'Mobile', width: 140, sortable: true, filter: true },
      { field: 'status', headerName: 'Status', width: 120, sortable: true, filter: true }
    );
    return cols;
  }

  load(): void {
    this.loading = true;
    this.umUserService
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

  onRowDoubleClicked(event: RowDoubleClickedEvent<GetUserResponse>): void {
    const id = event.data?.id;
    if (id != null) {
      this.router.navigate(['/um', 'user', id]);
    }
  }

  goNew(): void {
    this.router.navigate(['/um', 'user', 'new']);
  }
}
