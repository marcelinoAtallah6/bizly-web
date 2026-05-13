import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, catchError, map, of } from 'rxjs';
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
 * Navbar / shell profile from the access JWT only (no UM {@code /user/get}), so layout works even when
 * the user lacks permission to open User Management. After self-service edits, call {@link refresh(true)}
 * once a new access token is in place (e.g. after {@link AuthService.refreshToken}).
 *
 * When the JWT can't embed the avatar (image too large for the token), we transparently fall back to
 * {@code POST /auth/me/avatar} and cache the result against the access-token tail so the next refresh
 * doesn't trigger another network call.
 */
@Injectable({
  providedIn: 'root',
})
export class UserProfileService {
  private readonly subject = new BehaviorSubject<NavbarProfileView | null>(null);
  readonly profile$ = this.subject.asObservable();

  private cachedUserId: number | null = null;
  private cachedTokenTail = '';
  /** Resolved-avatar cache keyed by token tail so a token refresh re-validates without re-fetching forever. */
  private cachedAvatarUrl: string | null = null;
  private cachedAvatarTokenTail = '';
  /** Prevents flapping HTTP calls when the JWT genuinely has no image (oversized but DB column empty). */
  private avatarFetchAttempted = false;
  private avatarFetchAttemptedTokenTail = '';

  constructor(private readonly auth: AuthService) {}

  refresh(force = false): Observable<NavbarProfileView | null> {
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

    // If JWT carried the avatar, we're done. Otherwise check for a cached fetched URL bound to the
    // same token tail. Only when both miss do we hit the server.
    if (baseView.avatarUrl) {
      this.subject.next(baseView);
      return of(baseView);
    }
    if (this.cachedAvatarUrl && this.cachedAvatarTokenTail === tail) {
      const merged = { ...baseView, avatarUrl: this.cachedAvatarUrl };
      this.subject.next(merged);
      return of(merged);
    }
    if (claims.profileImageInJwt === false || claims.profileImageOversized === true) {
      // Don't replay the network call on every navigation if the user simply has no avatar.
      if (this.avatarFetchAttempted && this.avatarFetchAttemptedTokenTail === tail) {
        this.subject.next(baseView);
        return of(baseView);
      }
      this.subject.next(baseView);
      return this.auth.fetchMyAvatar().pipe(
        map((resp) => {
          this.avatarFetchAttempted = true;
          this.avatarFetchAttemptedTokenTail = tail;
          const data = resp?.data;
          let url: string | null = null;
          if (data && data.mime && data.base64) {
            url = `data:${data.mime};base64,${data.base64}`;
          }
          this.cachedAvatarUrl = url;
          this.cachedAvatarTokenTail = tail;
          const view: NavbarProfileView = { ...baseView, avatarUrl: url };
          this.subject.next(view);
          return view;
        }),
        catchError(() => {
          this.avatarFetchAttempted = true;
          this.avatarFetchAttemptedTokenTail = tail;
          return of(baseView);
        })
      );
    }

    this.subject.next(baseView);
    return of(baseView);
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

  private mapClaims(c: JwtAccessClaims): NavbarProfileView {
    const displayName =
      [c.firstName, c.lastName].filter(Boolean).join(' ').trim() || c.username || 'User';
    let avatarUrl: string | null = null;
    if (c.profileImageInJwt && c.profileImageMime && c.profileImageBase64) {
      avatarUrl = `data:${c.profileImageMime};base64,${c.profileImageBase64}`;
    }
    return {
      displayName,
      subtitle: c.email ?? '',
      avatarUrl,
    };
  }
}
