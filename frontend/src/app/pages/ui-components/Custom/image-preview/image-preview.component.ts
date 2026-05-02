// image-preview-dialog.component.ts
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'custom-image-preview-dialog',
  template: `
    <div class="image-preview-container" (click)="onContainerClick()">

      <button mat-menu-item class="close-button" (click)="closeDialog()"> 
          <span>close</span>
      </button>
      
      <img [src]="data.imageUrl" [alt]="data.title" class="preview-image">
      
      <div class="image-caption" *ngIf="data.title">
        {{ data.title }}
      </div>
    </div>
  `,
  styles: [`
    .image-preview-container {
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background-color: rgba(0, 0, 0, 0.9);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 1000;
    }

    .close-button {
      position: absolute;
      top: 20px;
      right: 20px;
      color: white;
      z-index: 1001;
    }

    .preview-image {
      max-width: 90vw;
      max-height: 90vh;
      object-fit: contain;
      cursor: zoom-out;
    }

    .image-caption {
      position: absolute;
      bottom: 20px;
      left: 0;
      right: 0;
      text-align: center;
      color: white;
      padding: 10px;
      background-color: rgba(0, 0, 0, 0.5);
      font-size: 16px;
    }
  `]
})
export class ImagePreviewDialogComponent {

  constructor(public dialogRef: MatDialogRef<ImagePreviewDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: { imageUrl: string; title: string }) {
    this.dialogRef.keydownEvents().subscribe(event => {
      if (event.key === 'Escape') {
        this.closeDialog();
      }
    });

  }

  onContainerClick(): void {
    // Close dialog logic here
  }

  closeDialog(): void {
    this.dialogRef.close();
  }
}