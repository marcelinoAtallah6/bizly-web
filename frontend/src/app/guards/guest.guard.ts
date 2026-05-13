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
 * Allows access to login/register only when the user is not authenticated.
 * If already authenticated, redirects to the main app entry (dashboard).
 */
@Injectable({
  providedIn: 'root',
})
export class GuestGuard implements CanActivate, CanActivateChild {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  canActivate(_route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    return this.allowOnlyGuests(state.url);
  }

  canActivateChild(_route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    return this.allowOnlyGuests(state.url);
  }

  /**
   * Routes are exempt from the guest check when they are post-login onboarding screens — the welcome
   * wizard and business registration only make sense while authenticated. Without this exemption an
   * authenticated user who lands on /authentication/welcome would be bounced to /dashboard,
   * defeating the whole onboarding flow.
   */
  private static readonly POST_LOGIN_PATHS = ['/authentication/welcome', '/authentication/register-business'];

  private allowOnlyGuests(url: string): boolean | UrlTree {
    const isAuthed = this.authService.isAuthenticated();
    if (!isAuthed) return true;
    if (GuestGuard.POST_LOGIN_PATHS.some((p) => url.startsWith(p))) {
      return true;
    }
    return this.router.parseUrl('/dashboard');
  }
}
