import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { GetUserResponse } from 'src/app/core/models/um.models';
import { UmUserService } from 'src/app/pages/um/services/um-user.service';
import { AuthService } from './auth.service';

export interface NavbarProfileView {
  displayName: string;
  subtitle: string;
  avatarUrl: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class UserProfileService {
  private readonly subject = new BehaviorSubject<NavbarProfileView | null>(null);
  readonly profile$ = this.subject.asObservable();

  constructor(
    private readonly auth: AuthService,
    private readonly umUser: UmUserService
  ) {}

  /** Loads UM user (including optional profile image) for the logged-in JWT user. */
  refresh(): void {
    const claims = this.auth.getAccessTokenClaims();
    if (!claims) {
      this.subject.next(null);
      return;
    }
    const fallbackName = [claims.firstName, claims.lastName].filter(Boolean).join(' ').trim() || 'User';
    this.umUser.get({ id: claims.userId }).subscribe({
      next: (u) => this.subject.next(this.mapUser(u, fallbackName)),
      error: () =>
        this.subject.next({
          displayName: fallbackName,
          subtitle: '',
          avatarUrl: null,
        }),
    });
  }

  clear(): void {
    this.subject.next(null);
  }

  private mapUser(u: GetUserResponse, fallbackName: string): NavbarProfileView {
    const displayName =
      [u.firstName, u.lastName].filter(Boolean).join(' ').trim() || u.username || fallbackName;
    let avatarUrl: string | null = null;
    if (u.profileImageBase64 && u.profileImageMimeType) {
      avatarUrl = `data:${u.profileImageMimeType};base64,${u.profileImageBase64}`;
    }
    return {
      displayName,
      subtitle: u.email ?? '',
      avatarUrl,
    };
  }
}
