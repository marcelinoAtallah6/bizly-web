import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  CanActivateChild,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { MenuPermAction, MenuPermissionService } from '../services/menu-permission.service';

/**
 * Optional descriptor for {@link Route#data} when you need to override the inferred action — e.g.
 * a form route that is the same URL pattern but should require {@code 'add'} or {@code 'edit'}.
 * The route to check is always taken from the navigated URL, never declared statically.
 */
export type RoutePermission =
  | MenuPermAction
  | {
      action?: MenuPermAction;
      /** When {@code false}, routes not yet listed in UM matrix are allowed through (legacy fallback). */
      strict?: boolean;
    };

/**
 * Route guard that gates feature screens off the role's JWT permission matrix only — no static
 * route-to-permission map. The URL being navigated to is matched against {@code perms[].r} via
 * longest-prefix in {@link MenuPermissionService}, so a token row for {@code /pm/products} grants
 * {@code /pm/products}, {@code /pm/products/add}, {@code /pm/products/:id/edit} automatically.
 *
 * Default action is inferred from the URL itself: paths ending with {@code /add} or {@code /new}
 * require {@code 'add'}, paths containing {@code /edit} require {@code 'edit'}, everything else
 * requires {@code 'view'}. Override on a per-route basis with {@code data.permission = 'edit'} or
 * {@code data.permission = { action: 'edit', strict: false }}.
 *
 * If denied, the user is redirected to the first JWT-accessible screen — falling back to
 * {@code /dashboard} when nothing else is available — so manual URL entry can never reveal a
 * forbidden screen and never traps the user on an empty page.
 */
@Injectable({ providedIn: 'root' })
export class PermissionGuard implements CanActivate, CanActivateChild {
  constructor(
    private readonly perm: MenuPermissionService,
    private readonly router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    return this.check(route, state.url);
  }

  canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    return this.check(route, state.url);
  }

  private check(route: ActivatedRouteSnapshot, url: string): boolean | UrlTree {
    if (!this.perm.isMatrixActive()) {
      return true;
    }
    const cleaned = this.stripQueryFragment(url);
    if (!cleaned) {
      return true;
    }
    const { action, strict } = this.resolveDescriptor(route, cleaned);
    const allowed = strict
      ? this.perm.can(cleaned, action)
      : this.perm.canIfListedOrAllow(cleaned, action);
    if (allowed) {
      return true;
    }
    return this.router.parseUrl(this.computeFallback(route));
  }

  /** Reads {@code data.permission} from the route tree (deepest leaf wins) and merges with URL inference. */
  private resolveDescriptor(
    route: ActivatedRouteSnapshot,
    url: string
  ): { action: MenuPermAction; strict: boolean } {
    let raw: RoutePermission | undefined;
    let current: ActivatedRouteSnapshot | null = route;
    while (current && raw === undefined) {
      raw = current.data?.['permission'] as RoutePermission | undefined;
      current = current.firstChild ?? null;
    }
    if (raw === undefined) {
      raw = route.data?.['permission'] as RoutePermission | undefined;
    }
    const inferred = this.inferActionFromUrl(url);
    if (raw == null) {
      return { action: inferred, strict: true };
    }
    if (typeof raw === 'string') {
      return { action: raw, strict: true };
    }
    return {
      action: raw.action ?? inferred,
      strict: raw.strict !== false,
    };
  }

  private inferActionFromUrl(url: string): MenuPermAction {
    const u = url.toLowerCase();
    if (/\/(add|new)(\/|$)/.test(u)) {
      return 'add';
    }
    if (/\/edit(\/|$)/.test(u)) {
      return 'edit';
    }
    return 'view';
  }

  private stripQueryFragment(url: string): string {
    let u = (url ?? '').trim();
    if (!u) {
      return '';
    }
    const q = u.indexOf('?');
    if (q >= 0) {
      u = u.substring(0, q);
    }
    const h = u.indexOf('#');
    if (h >= 0) {
      u = u.substring(0, h);
    }
    return u;
  }

  /**
   * When the guard denies a screen, take the user to a screen they CAN see — typically the first
   * accessible row in the JWT matrix, otherwise {@code /dashboard}. {@code data.permissionFallback}
   * on the route still wins when set, so feature modules can override this if they need to.
   */
  private computeFallback(route: ActivatedRouteSnapshot): string {
    const declared = this.readString(route, 'permissionFallback');
    if (declared) {
      return declared;
    }
    return this.perm.firstAccessibleRoute() ?? '/dashboard';
  }

  private readString(route: ActivatedRouteSnapshot, key: string): string | null {
    let current: ActivatedRouteSnapshot | null = route;
    while (current) {
      const v = current.data?.[key];
      if (typeof v === 'string' && v.trim().length > 0) {
        return v.trim();
      }
      current = current.parent ?? null;
    }
    return null;
  }
}
