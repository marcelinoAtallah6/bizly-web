import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from 'src/app/services/auth.service';

/**
 * Restricts routes to the portal root administrator ({@code tenantBypass} in JWT).
 * Delegated ADMIN roles must be granted explicit menu permissions instead.
 */
@Injectable({ providedIn: 'root' })
export class PortalRootAdminGuard implements CanActivate {
  constructor(
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly snack: MatSnackBar
  ) {}

  canActivate(): boolean {
    if (this.auth.isJwtTenantBypass()) {
      return true;
    }
    this.snack.open('This screen is restricted to portal root administrators.', 'Close', {
      duration: 5000,
    });
    void this.router.navigate(['/dashboard']);
    return false;
  }
}
