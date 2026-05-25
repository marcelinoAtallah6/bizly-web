import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, map, of, switchMap } from 'rxjs';
import { AuthService, JwtAccessClaims } from './auth.service';

export interface NavbarProfileView {
  displayName: string;
  subtitle: string;
  avatarUrl: string | null;
}

function tokenTail(token: string | null): string {
  if (!token) {
    return '';
  }
  return token.length > 64 ? token.slice(-64) : token;
}

/**
 * Navbar profile for the shell header. Resolves the avatar from the JWT when embedded;
 * otherwise loads it from {@code POST /auth/me/avatar} (then {@code /auth/me} as fallback).
 *
 * Call {@link refresh} after login — it subscribes internally so the header does not need to.
 * Use {@link refreshAndWait} inside Rx pipelines (e.g. after saving My profile).
 */
@Injectable({
  providedIn: 'root',
})
export class UserProfileService {
  private readonly subject = new BehaviorSubject<NavbarProfileView | null>(null);
  readonly profile$ = this.subject.asObservable();

  private cachedUserId: number | null = null;
  private cachedTokenTail = '';
  private cachedAvatarUrl: string | null = null;
  private cachedAvatarTokenTail = '';
  private avatarFetchAttempted = false;
  private avatarFetchAttemptedTokenTail = '';

  constructor(private readonly auth: AuthService) {}

  /** Loads profile + avatar; safe to call without subscribing to the return value. */
  refresh(force = false): void {
    this.loadProfile(force).subscribe({ error: () => {} });
  }

  /** Same as {@link refresh} but returns an observable for {@code switchMap} chains. */
  refreshAndWait(force = false): Observable<NavbarProfileView | null> {
    return this.loadProfile(force);
  }

  clear(): void {
    this.cachedUserId = null;
    this.cachedTokenTail = '';
    this.cachedAvatarUrl = null;
    this.cachedAvatarTokenTail = '';
    this.avatarFetchAttempted = false;
    this.avatarFetchAttemptedTokenTail = '';
    this.subject.next(null);
  }

  private loadProfile(force: boolean): Observable<NavbarProfileView | null> {
    const claims = this.auth.getAccessTokenClaims();
    const access = this.auth.getAccessToken();
    const tail = tokenTail(access);

    if (!claims) {
      this.cachedUserId = null;
      this.cachedTokenTail = '';
      this.cachedAvatarUrl = null;
      this.cachedAvatarTokenTail = '';
      this.avatarFetchAttempted = false;
      this.avatarFetchAttemptedTokenTail = '';
      this.subject.next(null);
      return of(null);
    }

    if (!force && this.cachedUserId === claims.userId && this.cachedTokenTail === tail && this.subject.value) {
      return of(this.subject.value);
    }

    const baseView = this.mapClaims(claims);
    this.cachedUserId = claims.userId;
    this.cachedTokenTail = tail;

    if (baseView.avatarUrl) {
      this.subject.next(baseView);
      return of(baseView);
    }
    if (this.cachedAvatarUrl && this.cachedAvatarTokenTail === tail) {
      const merged = { ...baseView, avatarUrl: this.cachedAvatarUrl };
      this.subject.next(merged);
      return of(merged);
    }
    if (this.avatarFetchAttempted && this.avatarFetchAttemptedTokenTail === tail) {
      this.subject.next(baseView);
      return of(baseView);
    }

    this.subject.next(baseView);
    return this.fetchAvatarFromServer(baseView, tail);
  }

  private fetchAvatarFromServer(
    baseView: NavbarProfileView,
    tail: string
  ): Observable<NavbarProfileView | null> {
    return this.auth.fetchMyAvatar().pipe(
      switchMap((resp) => {
        const fromAvatar = this.avatarDataUrl(resp?.data?.mime, resp?.data?.base64);
        if (fromAvatar) {
          return of(this.applyAvatar(baseView, fromAvatar, tail));
        }
        return this.auth.fetchMe().pipe(
          map((me) => {
            const d = me?.data;
            const fromMe = this.avatarDataUrl(d?.profileImageMime, d?.profileImageBase64);
            return this.applyAvatar(baseView, fromMe, tail);
          })
        );
      }),
      catchError(() =>
        this.auth.fetchMe().pipe(
          map((me) => {
            const d = me?.data;
            const fromMe = this.avatarDataUrl(d?.profileImageMime, d?.profileImageBase64);
            return this.applyAvatar(baseView, fromMe, tail);
          }),
          catchError(() => {
            this.avatarFetchAttempted = true;
            this.avatarFetchAttemptedTokenTail = tail;
            return of(baseView);
          })
        )
      )
    );
  }

  private applyAvatar(
    baseView: NavbarProfileView,
    url: string | null,
    tail: string
  ): NavbarProfileView {
    this.avatarFetchAttempted = true;
    this.avatarFetchAttemptedTokenTail = tail;
    this.cachedAvatarUrl = url;
    this.cachedAvatarTokenTail = tail;
    const view: NavbarProfileView = { ...baseView, avatarUrl: url };
    this.subject.next(view);
    return view;
  }

  private avatarDataUrl(mime?: string | null, base64?: string | null): string | null {
    if (!mime?.trim() || !base64?.trim()) {
      return null;
    }
    return `data:${mime.trim()};base64,${base64.trim()}`;
  }

  private mapClaims(c: JwtAccessClaims): NavbarProfileView {
    const displayName =
      [c.firstName, c.lastName].filter(Boolean).join(' ').trim() || c.username || 'User';
    const avatarUrl = this.avatarDataUrl(c.profileImageMime, c.profileImageBase64);
    return {
      displayName,
      subtitle: c.email ?? '',
      avatarUrl,
    };
  }
}
