import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
} from '@angular/common/http';
import { Injectable, NgZone } from '@angular/core';
import { Router } from '@angular/router';
import {
  Observable,
  catchError,
  defer,
  finalize,
  of,
  shareReplay,
  switchMap,
  take,
  tap,
  throwError,
} from 'rxjs';
import { AuthService } from '../services/auth.service';
import { DeviceIdService } from '../services/device-id.service';
import { SessionPromptService } from '../services/session-prompt.service';

@Injectable()
export class CustomHTTPInterceptor implements HttpInterceptor {
  private refreshInFlight$: Observable<string> | null = null;

  /**
   * Public auth endpoints. We never attach a Bearer token to these calls and we
   * never run the 401-refresh cascade on them. Two reasons:
   *
   *   1. The gateway treats them as anonymous; sending a stale JWT alongside the
   *      request would still pass through but every error response surfaced
   *      back to the SPA would be misinterpreted as "your session expired",
   *      kicking the user out of the registration / forgot-password flow.
   *   2. The 401 handler ends with {@code router.navigate('/authentication/login')}.
   *      If a user clicks "Create an account" while a stale token is in
   *      {@code localStorage}, the GET on {@code /auth/business-types} would
   *      otherwise bounce them right back to the login screen.
   *
   * Keep this list in sync with {@code SecurityConfig.permitAll(...)} and the
   * gateway's {@code PUBLIC_PATHS} so it is a single source of truth.
   */
  private static readonly PUBLIC_AUTH_PATHS = [
    '/auth/login',
    '/auth/refresh',
    '/auth/logout',
    '/auth/register',
    '/auth/business-types',
    '/auth/forgot-password',
    '/auth/social/',
  ];

  constructor(
    private readonly authService: AuthService,
    private readonly deviceIdService: DeviceIdService,
    private readonly sessionPrompt: SessionPromptService,
    private readonly router: Router,
    private readonly ngZone: NgZone
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const isPublic = this.isPublicAuthEndpoint(req.url);
    const authReq = this.addAuthHeaders(req, undefined, isPublic);

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        const isUnauthorized = error.status === 401;

        // Never trigger the session-expired cascade for public auth endpoints —
        // the user has no session yet (or is in the middle of starting one).
        if (!isUnauthorized || isPublic) {
          return throwError(() => error);
        }

        return this.handle401Error(req, next);
      })
    );
  }

  private forceSignOut(): void {
    this.authService.clearSession();
    this.ngZone.run(() => this.router.navigate(['/authentication/login']));
  }

  private isPublicAuthEndpoint(url: string): boolean {
    const pathOnly = url.split('?')[0];
    // Must be exact: url.includes('/auth/register') would match /auth/register-business
    // (authenticated) and skip the Bearer token → gateway 401 / api-auth "Invalid session".
    return CustomHTTPInterceptor.PUBLIC_AUTH_PATHS.some((p) =>
      p === '/auth/register' ? pathOnly.endsWith('/auth/register') : url.includes(p)
    );
  }

  private handle401Error(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.refreshInFlight$) {
      this.refreshInFlight$ = defer(() => {
        const canRefresh =
          !!this.authService.getRefreshToken() && !!this.authService.getSessionId();
        if (!canRefresh) {
          return throwError(() => new Error('No refresh token'));
        }
        if (this.authService.isSessionExtendGraceExceeded()) {
          return throwError(() => new Error('Session extend grace period exceeded'));
        }
        return this.sessionPrompt.askExtendSession().pipe(
          switchMap((extend) => {
            if (!extend) {
              return throwError(() => new Error('Session extension declined'));
            }
            return this.authService.refreshToken().pipe(
              switchMap((response) => {
                const token = response?.data?.token ?? null;
                if (!token) {
                  return throwError(() => new Error('No access token from refresh'));
                }
                return of(token);
              })
            );
          })
        );
      }).pipe(
        tap({
          error: () => this.forceSignOut(),
        }),
        finalize(() => {
          this.refreshInFlight$ = null;
        }),
        shareReplay({ bufferSize: 1, refCount: false })
      );
    }

    return this.refreshInFlight$.pipe(
      take(1),
      switchMap((token) => next.handle(this.addAuthHeaders(req, token))),
      catchError((err) => throwError(() => err))
    );
  }

  private addAuthHeaders(
    req: HttpRequest<any>,
    forcedToken?: string,
    isPublic: boolean = this.isPublicAuthEndpoint(req.url)
  ): HttpRequest<any> {
    const token = forcedToken ?? this.authService.getAccessToken();
    const deviceId = this.deviceIdService.getDeviceId();

    const headers: Record<string, string> = {};

    // Skip Authorization on public endpoints — a stale token attached here
    // would surface as "session expired" to fresh visitors who actually have
    // no session, breaking the register / forgot-password flows.
    if (token && !isPublic) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    if (deviceId && !req.url.includes('/auth/login')) {
      headers['X-DEVICE-ID'] = deviceId;
    }

    // SUPER_ADMIN "act-as" header — the downstream InternalAuthFilter only honours it
    // when the caller's role-level (carried in the JWT) is ADMIN. A tampered SPA cannot
    // bypass tenancy because the role-level claim is signed.
    const adminCtx = this.authService.getAdminBusinessContext();
    if (adminCtx != null) {
      headers['X-Business-Override'] = String(adminCtx);
    }

    if (Object.keys(headers).length === 0) {
      return req;
    }

    return req.clone({
      setHeaders: headers,
    });
  }
}
