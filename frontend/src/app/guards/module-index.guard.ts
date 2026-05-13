import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { MenuPermissionService } from '../services/menu-permission.service';

/**
 * Index-redirect guard for feature modules. Attach to the empty-path child of a feature route
 * with {@code data.moduleIndex = '/bm'} (the URL prefix). The guard returns a {@link UrlTree}
 * pointing at the first JWT-accessible sub-route under that prefix so a role with only
 * {@code /um/role} (but not {@code /um/user}) still lands on {@code /um/role} instead of being
 * bounced to the dashboard by {@link PermissionGuard}.
 *
 * When the role has no accessible child under the prefix, falls back to the first accessible
 * top-level route, then {@code /dashboard} as a last resort.
 */
@Injectable({ providedIn: 'root' })
export class ModuleIndexGuard implements CanActivate {
  constructor(
    private readonly perm: MenuPermissionService,
    private readonly router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, _state: RouterStateSnapshot): boolean | UrlTree {
    const prefix = String(route.data?.['moduleIndex'] ?? '').trim();
    const target =
      (prefix ? this.perm.firstAccessibleRoute(prefix) : null) ??
      this.perm.firstAccessibleRoute() ??
      '/dashboard';
    return this.router.parseUrl(target);
  }
}
