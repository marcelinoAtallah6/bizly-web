import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

export interface AlertDialogData {
  title: string;
  message: string;
  alertType: 'info' | 'success' | 'warn' | 'error' | 'confirm';
  confirmButtonText?: string;
  cancelButtonText?: string;
  showCancelButton?: boolean;
}


@Component({
  selector: 'custom-alert',
  templateUrl: './alert.component.html',
  styleUrl: './alert.component.scss'
})
export class AlertComponent {
  constructor(
    public dialogRef: MatDialogRef<AlertComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AlertDialogData
  ) { }

  onConfirm(): void {
    this.dialogRef.close(true); // Return true on confirm
  }

  onCancel(): void {
    this.dialogRef.close(false); // Return false on cancel
  }
}
