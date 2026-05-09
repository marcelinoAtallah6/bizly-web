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

  canActivate(_route: ActivatedRouteSnapshot, _state: RouterStateSnapshot): boolean | UrlTree {
    return this.allowOnlyGuests();
  }

  canActivateChild(_route: ActivatedRouteSnapshot, _state: RouterStateSnapshot): boolean | UrlTree {
    return this.allowOnlyGuests();
  }

  private allowOnlyGuests(): boolean | UrlTree {
    if (this.authService.isAuthenticated()) {
      return this.router.parseUrl('/dashboard');
    }
    return true;
  }
}
