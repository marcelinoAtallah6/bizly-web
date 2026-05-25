import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';

/**
 * Shown while {@code um_business.status = PENDING_APPROVAL}. The session is intentionally
 * read-only (JWT omits menu permissions); only sign-out is offered here.
 */
@Component({
  selector: 'app-pending-business-approval',
  templateUrl: './pending-business-approval.component.html',
  styleUrls: ['./pending-business-approval.component.scss'],
})
export class PendingBusinessApprovalComponent {
  loggingOut = false;

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router
  ) {}

  logout(): void {
    if (this.loggingOut) return;
    this.loggingOut = true;
    this.auth.logout().subscribe({
      next: () => this.router.navigateByUrl('/authentication/login'),
      error: () => {
        this.auth.clearSession();
        this.router.navigateByUrl('/authentication/login');
      },
    });
  }
}
