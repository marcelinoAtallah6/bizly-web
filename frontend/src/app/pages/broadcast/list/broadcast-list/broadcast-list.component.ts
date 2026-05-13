import { Component, OnInit } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { NgZone } from '@angular/core';
import { ColDef, GridReadyEvent, ICellRendererParams } from 'ag-grid-community';
import { finalize } from 'rxjs';
import { GetBroadcastResponse } from 'src/app/core/models/broadcast.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { SimpleConfirmDialogComponent } from 'src/app/shared/dialogs/simple-confirm-dialog.component';
import { BroadcastPreviewDialogComponent } from '../../dialogs/broadcast-preview-dialog.component';
import { BroadcastMessageService } from '../../services/broadcast-message.service';

export interface BroadcastListGridContext {
  previewRow: (row: GetBroadcastResponse) => void;
  tryQueueSend: (row: GetBroadcastResponse) => void;
  canQueueSend: (row: GetBroadcastResponse) => boolean;
}

@Component({
  selector: 'app-broadcast-list',
  templateUrl: './broadcast-list.component.html',
  styleUrl: './broadcast-list.component.scss',
})
export class BroadcastListComponent implements OnInit {
  rowData: GetBroadcastResponse[] = [];
  loading = false;
  totalCount = 0;
  pageNumber = 0;
  pageSize = 25;

  readonly routeBroadcast = '/broadcast';

  readonly gridContext: BroadcastListGridContext = {
    // ag-Grid cellRenderer click handlers run outside Angular zone — re-enter
    // so dialogs render bindings and navigation works consistently.
    previewRow: (row) => this.ngZone.run(() => this.openPreviewForRow(row)),
    tryQueueSend: (row) => this.ngZone.run(() => this.confirmAndSend(row)),
    canQueueSend: (row) => this.canQueueSend(row),
  };

  columnDefs: ColDef<GetBroadcastResponse>[] = [
    { field: 'id', headerName: 'ID', width: 88, sortable: true, filter: true },
    { field: 'subject', headerName: 'Subject', flex: 1.5, minWidth: 160, sortable: true, filter: true },
    { field: 'targetType', headerName: 'Target', width: 150, sortable: true, filter: true },
    { field: 'status', headerName: 'Status', width: 120, sortable: true, filter: true },
    {
      field: 'deliveryRequested',
      headerName: 'Queued',
      width: 100,
      sortable: true,
      filter: true,
      valueFormatter: (p) => (p.value === true ? 'Yes' : 'No'),
    },
    { field: 'createdBy', headerName: 'Created by', width: 130, sortable: true, filter: true },
    { field: 'createdAt', headerName: 'Created', width: 170, sortable: true, filter: true },
    {
      colId: 'actions',
      headerName: '',
      width: 112,
      minWidth: 112,
      sortable: false,
      filter: false,
      suppressHeaderMenuButton: true,
      cellRenderer: broadcastListActionsCellRenderer,
    },
  ];

  defaultColDef: ColDef = {
    resizable: true,
    floatingFilter: true,
  };

  constructor(
    private readonly broadcastApi: BroadcastMessageService,
    private readonly router: Router,
    private readonly menuPerm: MenuPermissionService,
    private readonly dialog: MatDialog,
    private readonly ngZone: NgZone
  ) {}

  get listToolbar(): ToolbarButton[] {
    return [
      {
        id: 'compose',
        icon: 'add',
        tooltip: 'New broadcast',
        action: () => void this.router.navigate(['/broadcast/compose']),
        // Allow navigation even before the menu route is seeded into JWT matrix.
        disabled: this.loading || !this.menuPerm.canIfListedOrAllow(this.routeBroadcast, 'add'),
      },
      { id: 'refresh', icon: 'refresh', tooltip: 'Refresh', action: () => this.load(), disabled: this.loading },
    ];
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.broadcastApi
      .gets({ pageNumber: this.pageNumber, pageSize: this.pageSize })
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

  openPreviewForRow(row: GetBroadcastResponse): void {
    if (!this.menuPerm.canIfListedOrAllow(this.routeBroadcast, 'view')) {
      return;
    }
    this.broadcastApi.preview({ subject: row.subject, body: row.body }).subscribe({
      next: (res) => {
        this.dialog.open(BroadcastPreviewDialogComponent, {
          width: '600px',
          maxWidth: '95vw',
          data: res,
        });
      },
      error: () => {},
    });
  }

  canQueueSend(row: GetBroadcastResponse): boolean {
    if (!this.menuPerm.canIfListedOrAllow(this.routeBroadcast, 'add')) {
      return false;
    }
    const st = (row.status ?? '').toUpperCase();
    if (st !== 'PENDING') {
      return false;
    }
    if (row.deliveryRequested === true) {
      return false;
    }
    return true;
  }

  confirmAndSend(row: GetBroadcastResponse): void {
    if (!this.canQueueSend(row) || row.id == null) {
      return;
    }
    this.dialog
      .open(SimpleConfirmDialogComponent, {
        width: '420px',
        data: {
          title: 'Queue broadcast for delivery?',
          message: `Message #${row.id} will be picked up by the notification worker.`,
          confirmLabel: 'Queue send',
        },
      })
      .afterClosed()
      .subscribe((ok) => {
        if (!ok) {
          return;
        }
        this.broadcastApi.send({ id: row.id }).subscribe({
          next: () => this.load(),
          error: () => {},
        });
      });
  }
}

function broadcastListActionsCellRenderer(
  params: ICellRendererParams<GetBroadcastResponse, unknown, BroadcastListGridContext>
) {
  const wrap = document.createElement('div');
  wrap.className = 'broadcast-list-actions';

  const row = params.data;
  const ctx = params.context as BroadcastListGridContext | undefined;
  if (!row || !ctx) {
    return wrap;
  }

  const previewBtn = document.createElement('button');
  previewBtn.type = 'button';
  previewBtn.className = 'ag-grid-icon-action-btn';
  previewBtn.title = 'Preview rendered';
  previewBtn.setAttribute('aria-label', 'Preview rendered');
  previewBtn.innerHTML = '<span class="material-icons" aria-hidden="true">visibility</span>';
  previewBtn.addEventListener('click', (ev) => {
    ev.preventDefault();
    ev.stopPropagation();
    ctx.previewRow(row);
  });
  wrap.appendChild(previewBtn);

  if (ctx.canQueueSend(row)) {
    const sendBtn = document.createElement('button');
    sendBtn.type = 'button';
    sendBtn.className = 'ag-grid-icon-action-btn';
    sendBtn.title = 'Queue send';
    sendBtn.setAttribute('aria-label', 'Queue send');
    sendBtn.innerHTML = '<span class="material-icons" aria-hidden="true">send</span>';
    sendBtn.addEventListener('click', (ev) => {
      ev.preventDefault();
      ev.stopPropagation();
      ctx.tryQueueSend(row);
    });
    wrap.appendChild(sendBtn);
  }

  return wrap;
}

