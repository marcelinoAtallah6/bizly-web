import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable, map } from 'rxjs';
import { SessionExtendDialogComponent } from '../dialogs/session-extend-dialog/session-extend-dialog.component';

@Injectable({
  providedIn: 'root',
})
export class SessionPromptService {
  constructor(private readonly dialog: MatDialog) {}

  /**
   * Shows a modal asking whether to extend the session after access token expiry.
   * @returns Observable emitting true if user chose to extend, false otherwise.
   */
  askExtendSession(): Observable<boolean> {
    const ref = this.dialog.open(SessionExtendDialogComponent, {
      width: '420px',
      maxWidth: '95vw',
      disableClose: true,
      autoFocus: 'first-tabbable',
    });

    return ref.afterClosed().pipe(map((result) => result === true));
  }
}
