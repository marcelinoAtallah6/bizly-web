import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ColDef, GridReadyEvent, RowDoubleClickedEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { GetUserResponse } from 'src/app/core/models/um.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { UmUserService } from '../../services/um-user.service';

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.scss',
})
export class UserListComponent implements OnInit {
  rowData: GetUserResponse[] = [];
  loading = false;

  columnDefs: ColDef[] = [
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
    { field: 'email', headerName: 'Email', flex: 1, sortable: true, filter: true },
    { field: 'mobileNumber', headerName: 'Mobile', width: 140, sortable: true, filter: true },
    { field: 'status', headerName: 'Status', width: 120, sortable: true, filter: true },
  ];

  defaultColDef: ColDef = {
    resizable: true,
    floatingFilter: true,
  };

  constructor(
    private readonly umUserService: UmUserService,
    private readonly router: Router
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
      { id: 'add', icon: 'person_add', tooltip: 'New user', action: () => this.goNew(), color: 'primary' },
    ];
  }

  ngOnInit(): void {
    this.load();
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
