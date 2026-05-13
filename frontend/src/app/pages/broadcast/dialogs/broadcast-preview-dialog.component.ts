import { Component, Inject } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { PreviewBroadcastResponse } from 'src/app/core/models/broadcast.models';

@Component({
  selector: 'app-broadcast-preview-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Rendered preview</h2>
    <mat-dialog-content class="preview-dialog-body">
      <p><strong>Subject:</strong> {{ data.subject }}</p>
      <div class="preview-html" [innerHTML]="safeHtml"></div>
      <hr class="m-y-12" />
      <p class="preview-muted f-s-12 m-b-0"><strong>Plain text</strong></p>
      <pre class="preview-plain">{{ data.textBody }}</pre>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-flat-button mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `,
  styles: [
    `
      .preview-dialog-body {
        max-width: min(92vw, 560px);
        max-height: 70vh;
        overflow: auto;
        color: var(--textPrimary);
      }
      .preview-html {
        border: 1px solid var(--borderColor);
        border-radius: 4px;
        padding: 12px;
        background: var(--cardbg);
        color: var(--textPrimary);
      }
      .preview-muted {
        color: var(--textPrimary);
        opacity: 0.72;
      }
      .preview-plain {
        white-space: pre-wrap;
        font-size: 12px;
        margin: 0;
        color: var(--textPrimary);
      }
      .m-y-12 {
        margin: 12px 0;
      }
    `,
  ],
})
export class BroadcastPreviewDialogComponent {
  safeHtml: SafeHtml;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: PreviewBroadcastResponse,
    sanitizer: DomSanitizer
  ) {
    this.safeHtml = sanitizer.bypassSecurityTrustHtml(data.htmlBody ?? '');
  }
}

