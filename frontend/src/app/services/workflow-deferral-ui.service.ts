import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { WorkflowDeferralSuccessDialogComponent } from '../shared/workflow/workflow-deferral-success-dialog.component';

@Injectable({ providedIn: 'root' })
export class WorkflowDeferralUiService {
  /** If set, the next deferred (HTTP 202) success dialog will navigate here on OK. */
  private pendingNavigate: string[] | null = null;

  constructor(private readonly dialog: MatDialog) {}

  /** Call immediately before a mutating POST that may return HTTP 202 from the API gateway workflow filter. */
  setPendingNavigate(nav: string[] | null): void {
    this.pendingNavigate = nav;
  }

  openDeferredSubmissionSuccess(): void {
    const nav = this.pendingNavigate;
    this.pendingNavigate = null;
    this.dialog.open(WorkflowDeferralSuccessDialogComponent, {
      width: '420px',
      maxWidth: '95vw',
      disableClose: true,
      autoFocus: false,
      data: { navigateTo: nav },
    });
  }
}
