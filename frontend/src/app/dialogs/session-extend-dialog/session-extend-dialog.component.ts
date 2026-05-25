import { CommonModule } from '@angular/common';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

export interface SessionExtendDialogData {
  /** Time left before forced sign-out (ms). */
  graceRemainingMs: number;
  /** Full grace window (ms) — for display only. */
  graceTotalMs: number;
}

@Component({
  selector: 'app-session-extend-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatIconModule],
  templateUrl: './session-extend-dialog.component.html',
  styleUrl: './session-extend-dialog.component.scss',
})
export class SessionExtendDialogComponent implements OnInit, OnDestroy {
  remainingLabel = '';
  private timerId: ReturnType<typeof setInterval> | null = null;
  private remainingMs = 0;
  private closed = false;

  constructor(
    private readonly dialogRef: MatDialogRef<SessionExtendDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) private readonly data: SessionExtendDialogData
  ) {}

  ngOnInit(): void {
    this.remainingMs = Math.max(0, this.data?.graceRemainingMs ?? 0);
    this.updateRemainingLabel();
    if (this.remainingMs <= 0) {
      queueMicrotask(() => this.onDecline());
      return;
    }
    this.timerId = setInterval(() => {
      this.remainingMs = Math.max(0, this.remainingMs - 1000);
      this.updateRemainingLabel();
      if (this.remainingMs <= 0) {
        this.onDecline();
      }
    }, 1000);
  }

  ngOnDestroy(): void {
    this.clearTimer();
  }

  onExtend(): void {
    if (this.closed) {
      return;
    }
    this.closed = true;
    this.clearTimer();
    this.dialogRef.close(true);
  }

  onDecline(): void {
    if (this.closed) {
      return;
    }
    this.closed = true;
    this.clearTimer();
    this.dialogRef.close(false);
  }

  private clearTimer(): void {
    if (this.timerId != null) {
      clearInterval(this.timerId);
      this.timerId = null;
    }
  }

  private updateRemainingLabel(): void {
    const totalSec = Math.ceil(this.remainingMs / 1000);
    const min = Math.floor(totalSec / 60);
    const sec = totalSec % 60;
    if (min > 0) {
      this.remainingLabel = `${min} min ${sec} sec`;
    } else {
      this.remainingLabel = `${sec} sec`;
    }
  }
}
