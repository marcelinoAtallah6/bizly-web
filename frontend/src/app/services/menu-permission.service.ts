import { Injectable } from '@angular/core';
import { UM_SCREEN_ROUTES } from '../common/GlobalConstants';
import { AuthService } from './auth.service';

export type UmScreenRoute = (typeof UM_SCREEN_ROUTES)[keyof typeof UM_SCREEN_ROUTES];

export type MenuPermAction = 'view' | 'add' | 'edit' | 'delete';

/**
 * UI hints from JWT {@code permMatrix} / {@code perms}; server still enforces every API call.
 */
@Injectable({ providedIn: 'root' })
export class MenuPermissionService {
  constructor(private readonly auth: AuthService) {}

  /** When false in JWT, legacy mode — show actions unless blocked elsewhere. */
  isMatrixActive(): boolean {
    return this.auth.getJwtPermMatrix();
  }

  can(route: UmScreenRoute | string, action: MenuPermAction): boolean {
    if (!this.auth.getJwtPermMatrix()) {
      return true;
    }
    const norm = this.normalizeRoute(route);
    const row = this.findRow(norm);
    if (!row) {
      return false;
    }
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

  private findRow(norm: string) {
    return this.auth.getJwtMenuPerms().find((x) => {
      if (x.r == null || String(x.r).trim() === '') {
        return false;
      }
      return this.normalizeRoute(String(x.r)) === norm;
    });
  }

  private normalizeRoute(r: string): string {
    const t = r.trim();
    if (!t) {
      return '';
    }
    return t.startsWith('/') ? t : `/${t}`;
  }
}
