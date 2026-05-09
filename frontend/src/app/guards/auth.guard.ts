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
 * Allows access only when the user has a stored access token.
 * Use on the main app shell and all authenticated feature routes.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate, CanActivateChild {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  canActivate(_route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    return this.requireAuth(state.url);
  }

  canActivateChild(_route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    return this.requireAuth(state.url);
  }

  private requireAuth(_attemptedUrl: string): boolean | UrlTree {
    if (this.authService.isAuthenticated()) {
      return true;
    }
    return this.router.parseUrl('/authentication/login');
  }
}
