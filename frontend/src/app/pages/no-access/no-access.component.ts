import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';

/**
 * Landing screen shown when the currently active role grants no UI surface. We
 * deliberately keep it dependency-free (no dashboard widgets, no API calls) so
 * a role with zero permissions cannot accidentally trigger 403s on bootstrap.
 *
 * Reached two ways:
 *   1. {@code PermissionGuard} redirects here when a screen is denied AND
 *      {@code firstAccessibleRoute()} returns null (nothing in the matrix).
 *   2. The post-login redirect detects an empty matrix and lands the user
 *      here instead of {@code /dashboard}.
 *
 * Admin-level users will never reach this page — they bypass the matrix.
 * If the user switches back to a permitted role, {@link MenuPermissionService#changes$}
 * fires and we route them to the first accessible screen automatically so the
 * empty state isn't a dead end.
 */
@Component({
  selector: 'app-no-access',
  templateUrl: './no-access.component.html',
  styleUrls: ['./no-access.component.scss'],
})
export class NoAccessComponent implements OnInit, OnDestroy {
  activeRole: string | null = null;

  private sub?: Subscription;

  constructor(
    private readonly authService: AuthService,
    private readonly menuPerm: MenuPermissionService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.activeRole = this.authService.getJwtActiveRole();
    this.sub = this.menuPerm.changes$.subscribe(() => {
      this.activeRole = this.authService.getJwtActiveRole();
      if (this.menuPerm.isAdminBypass()) {
        this.router.navigateByUrl('/dashboard');
        return;
      }
      const next = this.menuPerm.firstAccessibleRoute();
      if (next) {
        this.router.navigateByUrl(next);
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }
}
