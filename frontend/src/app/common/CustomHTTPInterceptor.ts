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

  constructor(
    private readonly authService: AuthService,
    private readonly deviceIdService: DeviceIdService,
    private readonly sessionPrompt: SessionPromptService,
    private readonly router: Router,
    private readonly ngZone: NgZone
  ) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const authReq = this.addAuthHeaders(req);

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        const isUnauthorized = error.status === 401;
        const isLoginRequest = req.url.includes('/auth/login');
        const isRefreshRequest = req.url.includes('/auth/refresh');

        if (!isUnauthorized || isLoginRequest || isRefreshRequest) {
          return throwError(() => error);
        }

        return this.handle401Error(req, next);
      })
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
          error: () => {
            this.authService.clearSession();
            this.ngZone.run(() => this.router.navigate(['/authentication/login']));
          },
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

  private addAuthHeaders(req: HttpRequest<any>, forcedToken?: string): HttpRequest<any> {
    const token = forcedToken ?? this.authService.getAccessToken();
    const deviceId = this.deviceIdService.getDeviceId();

    const headers: Record<string, string> = {};

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    if (deviceId && !req.url.includes('/auth/login')) {
      headers['X-DEVICE-ID'] = deviceId;
    }

    if (Object.keys(headers).length === 0) {
      return req;
    }

    return req.clone({
      setHeaders: headers,
    });
  }
}
