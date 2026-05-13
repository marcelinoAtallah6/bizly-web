import { Injectable } from '@angular/core';
import { Observable, defer, startWith } from 'rxjs';
import { AuthService, JwtMenuPermRow } from './auth.service';

export type MenuPermAction = 'view' | 'add' | 'edit' | 'delete';

/**
 * UI gate that derives everything from the access token's {@code permMatrix} / {@code perms} claims —
 * no static route maps, no equivalence tables. A permission row whose {@code r} is a path-segment
 * prefix of the URL being checked grants access for that URL (so a row for {@code /pm/products}
 * automatically covers {@code /pm/products/add}, {@code /pm/products/123/edit}, etc.). When several
 * rows match, the **longest prefix wins** — leaves can override parents without changing the matrix.
 *
 * The server still enforces every API call; this service only decides what to show.
 */
@Injectable({ providedIn: 'root' })
export class MenuPermissionService {
  /** Emits on subscribe and after every login / refresh / active-role switch. */
  readonly changes$: Observable<void> = defer(() => this.auth.roleRefresh$).pipe(startWith(undefined));

  constructor(private readonly auth: AuthService) {}

  /** When false in JWT, legacy mode — show everything (server still gates the APIs). */
  isMatrixActive(): boolean {
    return this.auth.getJwtPermMatrix();
  }

  /**
   * True when the role's JWT matrix grants {@code action} on the supplied URL. Prefix-matches against
   * any {@code perms[].r} the token carries. Unknown URLs (no matching row) are denied.
   */
  can(route: string, action: MenuPermAction = 'view'): boolean {
    if (!this.auth.getJwtPermMatrix()) {
      return true;
    }
    const row = this.findBestRow(route);
    return row ? this.rowAllows(row, action) : false;
  }

  /**
   * Permissive variant — grants when the matrix is on but the URL is not in any row yet. Useful for
   * brand-new screens that haven't been added to UM_MENUS / UM_ROLE_MENU_PERM. The API still 403s.
   */
  canIfListedOrAllow(route: string, action: MenuPermAction = 'view'): boolean {
    if (!this.auth.getJwtPermMatrix()) {
      return true;
    }
    const row = this.findBestRow(route);
    return row ? this.rowAllows(row, action) : true;
  }

  canAny(routes: ReadonlyArray<string>, action: MenuPermAction = 'view'): boolean {
    return !!routes?.length && routes.some((r) => this.can(r, action));
  }

  canAll(routes: ReadonlyArray<string>, action: MenuPermAction = 'view'): boolean {
    return !!routes?.length && routes.every((r) => this.can(r, action));
  }

  /** Lookup by menu id — useful for tabs that bind directly to {@code UM_MENUS.ID}. */
  canByMenuId(menuId: number, action: MenuPermAction = 'view'): boolean {
    if (!this.auth.getJwtPermMatrix()) {
      return true;
    }
    const row = this.auth.getJwtMenuPerms().find((x) => Number(x.m) === Number(menuId));
    return row ? this.rowAllows(row, action) : false;
  }

  /**
   * First JWT route the role can view, in matrix order, optionally restricted to children of a
   * given path. Used by {@code /um}, {@code /bm} index redirects so a role with one sub-screen
   * still lands somewhere instead of bouncing to the dashboard.
   */
  firstAccessibleRoute(parentPath?: string): string | null {
    if (!this.auth.getJwtPermMatrix()) {
      return null;
    }
    const parent = parentPath ? this.normalize(parentPath) : null;
    for (const row of this.auth.getJwtMenuPerms()) {
      if (!row.v) {
        continue;
      }
      const r = this.normalize(row.r ?? '');
      if (!r) {
        continue;
      }
      if (parent && !this.isPrefix(parent, r)) {
        continue;
      }
      return r;
    }
    return null;
  }

  private rowAllows(row: JwtMenuPermRow, action: MenuPermAction): boolean {
    switch (action) {
      case 'view':
        return !!row.v;
      case 'add':
        return !!row.a;
      case 'edit':
        return !!row.e;
      case 'delete':
        return !!row.d;
    }
  }

  /**
   * Walks the JWT perms array and returns the row whose normalized {@code r} is the longest prefix
   * of the target URL. {@code null} when nothing matches.
   */
  private findBestRow(route: string): JwtMenuPermRow | null {
    const target = this.normalize(route);
    if (!target) {
      return null;
    }
    let best: JwtMenuPermRow | null = null;
    let bestLen = -1;
    for (const row of this.auth.getJwtMenuPerms()) {
      const r = this.normalize(String(row.r ?? ''));
      if (!r) {
        continue;
      }
      if (!this.isPrefix(r, target)) {
        continue;
      }
      if (r.length > bestLen) {
        best = row;
        bestLen = r.length;
      }
    }
    return best;
  }

  /** True when {@code candidate} is the target URL or a path-segment prefix of it (no partial token matches). */
  private isPrefix(candidate: string, target: string): boolean {
    if (candidate === target) {
      return true;
    }
    if (!target.startsWith(candidate)) {
      return false;
    }
    return target.charAt(candidate.length) === '/';
  }

  /** Lowercases, strips query/fragment, drops trailing slashes, ensures leading slash. */
  private normalize(r: string): string {
    let t = String(r ?? '').trim();
    if (!t) {
      return '';
    }
    const hash = t.indexOf('#');
    if (hash >= 0) {
      t = t.substring(0, hash);
    }
    const q = t.indexOf('?');
    if (q >= 0) {
      t = t.substring(0, q);
    }
    t = t.startsWith('/') ? t : `/${t}`;
    if (t.length > 1) {
      t = t.replace(/\/+$/, '');
    }
    return t.toLowerCase();
  }
}
