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
 *
 * <p>Optional {@code data.moduleIndexDefault} (e.g. {@code "messages"}): used when
 * {@link MenuPermissionService#firstAccessibleRoute} is {@code null} (admin bypass) or returns
 * only the module prefix with no deeper path — so {@code /broadcast} can land on
 * {@code /broadcast/messages} instead of the global landing route.
 */
@Injectable({ providedIn: 'root' })
export class ModuleIndexGuard implements CanActivate {
  constructor(
    private readonly perm: MenuPermissionService,
    private readonly router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, _state: RouterStateSnapshot): boolean | UrlTree {
    const prefix = String(route.data?.['moduleIndex'] ?? '').trim();
    const defaultChild = String(route.data?.['moduleIndexDefault'] ?? '').trim();
    const normPrefix = this.normalizePathPrefix(prefix);

    let target: string | null = null;
    if (prefix) {
      target = this.perm.firstAccessibleRoute(prefix);
    }

    if (normPrefix && defaultChild) {
      const defaultUrl = `${normPrefix}/${defaultChild.replace(/^\/+/, '')}`;
      if (target == null || this.pathsEqual(target, normPrefix)) {
        target = defaultUrl;
      }
    }

    const finalTarget = target ?? this.perm.landingRoute();
    return this.router.parseUrl(finalTarget);
  }

  private normalizePathPrefix(p: string): string {
    let t = String(p ?? '').trim();
    if (!t) {
      return '';
    }
    if (!t.startsWith('/')) {
      t = `/${t}`;
    }
    return t.length > 1 ? t.replace(/\/+$/, '') : t;
  }

  private pathsEqual(a: string, b: string): boolean {
    return this.normalizePathForCompare(a) === this.normalizePathForCompare(b);
  }

  private normalizePathForCompare(p: string): string {
    let t = String(p ?? '').trim().toLowerCase();
    if (!t) {
      return '';
    }
    if (!t.startsWith('/')) {
      t = `/${t}`;
    }
    return t.length > 1 ? t.replace(/\/+$/, '') : t;
  }
}
