import { Component, Inject } from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Router } from '@angular/router';

export interface WorkflowDeferralSuccessDialogData {
  navigateTo: string[] | null;
}

@Component({
  selector: 'app-workflow-deferral-success-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  animations: [
    trigger('pop', [
      transition(':enter', [
        style({ opacity: 0, transform: 'scale(0.92)' }),
        animate('220ms ease-out', style({ opacity: 1, transform: 'scale(1)' })),
      ]),
    ]),
  ],
  template: `
    <div class="dlg" @pop>
      <div class="icon-wrap">
        <mat-icon class="ok-icon">check_circle</mat-icon>
      </div>
      <h2 mat-dialog-title class="title">Success</h2>
      <mat-dialog-content>
        <p class="msg">Your request has been submitted and is now pending approval.</p>
      </mat-dialog-content>
      <mat-dialog-actions align="end">
        <button mat-flat-button color="primary" type="button" (click)="close()">OK</button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [
    `
      .dlg {
        padding: 8px 0 0;
        text-align: center;
      }
      .icon-wrap {
        display: flex;
        justify-content: center;
        margin-bottom: 8px;
      }
      .ok-icon {
        font-size: 56px;
        width: 56px;
        height: 56px;
        color: #2e7d32;
      }
      .title {
        justify-content: center;
        font-size: 1.25rem;
        margin: 0;
        text-align: center;
      }
      .msg {
        margin: 0;
        line-height: 1.5;
        color: rgba(0, 0, 0, 0.72);
        text-align: center;
      }
    `,
  ],
})
export class WorkflowDeferralSuccessDialogComponent {
  constructor(
    private readonly ref: MatDialogRef<WorkflowDeferralSuccessDialogComponent>,
    private readonly router: Router,
    @Inject(MAT_DIALOG_DATA) public readonly data: WorkflowDeferralSuccessDialogData
  ) {}

  close(): void {
    const nav = this.data?.navigateTo;
    this.ref.close(true);
    if (nav && nav.length > 0) {
      void this.router.navigate(nav);
    }
  }
}
