import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid-community';
import { WorkflowInstanceRow } from '../../services/um-workflow.service';

interface WorkflowQueueActionHost {
  openPayload(row: WorkflowInstanceRow | undefined): void;
  approve(id: number): void;
  openReject(row: WorkflowInstanceRow): void;
}

@Component({
  selector: 'app-workflow-queue-actions-renderer',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, MatTooltipModule],
  template: `
    <div class="wf-actions">
      <button mat-icon-button type="button" matTooltip="View details" (click)="onView($event)">
        <mat-icon>visibility</mat-icon>
      </button>
      <button
        *ngIf="showApproveReject"
        mat-icon-button
        type="button"
        class="approve"
        matTooltip="Approve"
        (click)="onApprove($event)"
      >
        <mat-icon>check_circle</mat-icon>
      </button>
      <button
        *ngIf="showApproveReject"
        mat-icon-button
        type="button"
        class="reject"
        matTooltip="Reject"
        (click)="onReject($event)"
      >
        <mat-icon>cancel</mat-icon>
      </button>
    </div>
  `,
  styles: [
    `
      .wf-actions {
        display: flex;
        align-items: center;
        gap: 2px;
      }
      .approve mat-icon {
        color: #2e7d32;
      }
      .reject mat-icon {
        color: #c62828;
      }
    `,
  ],
})
export class WorkflowQueueActionsRendererComponent implements ICellRendererAngularComp {
  params!: ICellRendererParams<WorkflowInstanceRow>;
  showApproveReject = false;

  agInit(params: ICellRendererParams<WorkflowInstanceRow>): void {
    this.params = params;
    const row = params.data;
    this.showApproveReject = !!(row && row.canApprove);
  }

  refresh(params: ICellRendererParams<WorkflowInstanceRow>): boolean {
    this.agInit(params);
    return true;
  }

  private parent(): WorkflowQueueActionHost {
    return this.params.context as WorkflowQueueActionHost;
  }

  onView(ev: Event): void {
    ev.stopPropagation();
    const row = this.params.data;
    if (row) {
      this.parent().openPayload(row);
    }
  }

  onApprove(ev: Event): void {
    ev.stopPropagation();
    const row = this.params.data;
    if (row?.id != null) {
      this.parent().approve(row.id);
    }
  }

  onReject(ev: Event): void {
    ev.stopPropagation();
    const row = this.params.data;
    if (row) {
      this.parent().openReject(row);
    }
  }
}
