import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable, map } from 'rxjs';
import {
  SessionExtendDialogComponent,
  SessionExtendDialogData,
} from '../dialogs/session-extend-dialog/session-extend-dialog.component';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class SessionPromptService {
  constructor(
    private readonly dialog: MatDialog,
    private readonly auth: AuthService
  ) {}

  /**
   * Shows a modal asking whether to extend the session after access token expiry.
   * Auto-closes with sign-out when the grace window elapses.
   * @returns Observable emitting true if user chose to extend, false otherwise.
   */
  askExtendSession(): Observable<boolean> {
    const data: SessionExtendDialogData = {
      graceRemainingMs: this.auth.getSessionExtendGraceRemainingMs(),
      graceTotalMs: this.auth.getSessionExtendGraceMs(),
    };
    const ref = this.dialog.open(SessionExtendDialogComponent, {
      width: '420px',
      maxWidth: '95vw',
      disableClose: true,
      autoFocus: 'first-tabbable',
      data,
    });

    return ref.afterClosed().pipe(map((result) => result === true));
  }
}
