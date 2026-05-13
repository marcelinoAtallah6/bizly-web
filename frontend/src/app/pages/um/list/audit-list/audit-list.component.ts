import { Component, OnInit } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { ColDef, GridReadyEvent } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AuditLogRowResponse,
  GetsAuditLogsRequest,
  GetsAuditLogsResponse,
} from 'src/app/core/models/um.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Component({
  selector: 'app-audit-list',
  templateUrl: './audit-list.component.html',
  styleUrl: './audit-list.component.scss',
})
export class AuditListComponent implements OnInit {
  rowData: AuditLogRowResponse[] = [];
  loading = false;

  totalCount = 0;
  pageNumber = 0;
  pageSize = 25;

  columnDefs: ColDef[] = [
    { field: 'id', headerName: 'ID', width: 90, sortable: true, filter: true },
    { field: 'createdAt', headerName: 'When', width: 180, sortable: true, filter: true },
    { field: 'username', headerName: 'User', flex: 1, sortable: true, filter: true },
    { field: 'actionCode', headerName: 'Action', flex: 1, sortable: true, filter: true },
    { field: 'resourceType', headerName: 'Resource', width: 120, sortable: true, filter: true },
    { field: 'resourceId', headerName: 'Resource ID', width: 110, sortable: true, filter: true },
    { field: 'ipAddress', headerName: 'IP', width: 130, sortable: true, filter: true },
    {
      field: 'newValues',
      headerName: 'Payload / new',
      flex: 2,
      sortable: false,
      filter: true,
      valueFormatter: (p) => this.shortJson(p.value),
    },
  ];

  defaultColDef: ColDef = {
    resizable: true,
    floatingFilter: true,
  };

  constructor(private readonly businessApi: BusinessApiService) {}

  get listToolbar(): ToolbarButton[] {
    return [
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
    ];
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    const url = GlobalConstants.API_ENDPOINTS.um.audit.gets;
    const body: GetsAuditLogsRequest = {
      pageNumber: this.pageNumber,
      pageSize: this.pageSize,
    };
    this.businessApi
      .postEnvelope<GetsAuditLogsResponse>(url, body, 'errors')
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (page) => {
          const totalPages = page.totalPages ?? 0;
          if (totalPages === 0) {
            this.pageNumber = 0;
          } else if (this.pageNumber >= totalPages) {
            this.pageNumber = totalPages - 1;
            this.load();
            return;
          }
          this.rowData = page.items ?? [];
          this.totalCount = page.totalCount ?? 0;
        },
        error: () => {},
      });
  }

  onPage(ev: PageEvent): void {
    this.pageNumber = ev.pageIndex;
    this.pageSize = ev.pageSize;
    this.load();
  }

  onGridReady(_e: GridReadyEvent): void {}

  private shortJson(raw: unknown): string {
    if (raw == null || raw === '') {
      return '';
    }
    const s = typeof raw === 'string' ? raw : JSON.stringify(raw);
    return s.length > 160 ? `${s.slice(0, 157)}…` : s;
  }
}
