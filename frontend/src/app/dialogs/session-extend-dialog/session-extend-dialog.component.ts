import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-session-extend-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './session-extend-dialog.component.html',
  styleUrl: './session-extend-dialog.component.scss',
})
export class SessionExtendDialogComponent {
  constructor(
    private readonly dialogRef: MatDialogRef<SessionExtendDialogComponent, boolean>
  ) {}

  onExtend(): void {
    this.dialogRef.close(true);
  }

  onDecline(): void {
    this.dialogRef.close(false);
  }
}
