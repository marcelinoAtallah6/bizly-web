import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  CanActivateChild,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Safety-net guard. The PRIMARY sign-up flow now happens before login: the
 * "Create Account" wizard at {@code /authentication/register} collects both
 * the user details AND the business setup, and submits them in a single atomic
 * transaction to {@code POST /auth/register}. That endpoint returns a JWT
 * session, so a freshly registered user lands on the dashboard with both
 * {@code business_id} and {@code first_login = false}.
 *
 * Login itself is purely authentication — it never redirects to a registration
 * screen. This guard exists ONLY as a defence-in-depth fallback for the rare
 * accounts that ended up in an incomplete state:
 *   - Social-login on the Login screen for a brand-new email: the backend
 *     auto-provisions the user with no business; we must finish that one-time
 *     setup before letting them into the main app.
 *   - An admin manually created a user without immediately attaching them to a
 *     business.
 *
 * Behaviour:
 *   - {@code first_login = true} → {@code /authentication/welcome}
 *   - {@code business_id is null} AND role-level ≠ ADMIN
 *       → {@code /authentication/register-business}
 *   - Otherwise → allow.
 *
 * Uses the cache populated by login / refresh / register / /auth/me. SUPER_ADMIN
 * is intentionally exempt because the role has no tenant of its own.
 */
@Injectable({
  providedIn: 'root',
})
export class OnboardingGuard implements CanActivate, CanActivateChild {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  canActivate(_route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    return this.check(state.url);
  }

  canActivateChild(_route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    return this.check(state.url);
  }

  private check(url: string): boolean | UrlTree {
    if (!this.authService.isAuthenticated()) {
      return true; // AuthGuard handles the redirect to /authentication/login
    }
    if (url.startsWith('/authentication')) {
      return true; // never redirect the onboarding screens themselves
    }
    if (this.authService.getCachedFirstLogin() === true) {
      return this.router.parseUrl('/authentication/welcome');
    }
    const bid = this.authService.getCachedBusinessId();
    const level = (this.authService.getCachedRoleLevel() ?? '').toUpperCase();
    if (bid == null && level !== 'ADMIN') {
      return this.router.parseUrl('/authentication/register-business');
    }
    return true;
  }
}
