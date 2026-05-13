import { Injectable } from '@angular/core';

/**
 * Verified profile snapshot returned by a social provider. Used to pre-fill the
 * Create-Account wizard when the user clicks "Sign up with Google / Facebook".
 *
 * The {@code idToken} / {@code accessToken} fields are forwarded verbatim to the
 * backend so it can re-verify with the provider before issuing a Bizly JWT — we
 * never trust the profile fields client-side for authentication, only for UX.
 */
export interface SocialAuthResult {
  provider: 'GOOGLE' | 'FACEBOOK' | 'APPLE';
  idToken?: string | null;
  accessToken?: string | null;
  email: string;
  firstName: string;
  lastName: string;
  providerUserId: string;
}

/**
 * Configure these in environment.ts (or an injected config) before going to prod.
 * The values below are placeholders that intentionally fail loudly so a missing
 * config can't be deployed by accident.
 */
const GOOGLE_CLIENT_ID = '645145027382-rd3m9lvdlfi5rl2vaobg44eofuc7m9jq.apps.googleusercontent.com';
const FACEBOOK_APP_ID = '__YOUR_FACEBOOK_APP_ID__';

declare const google: {
  accounts: {
    id: {
      initialize: (cfg: {
        client_id: string;
        callback: (resp: { credential: string }) => void;
        ux_mode?: 'popup' | 'redirect';
        cancel_on_tap_outside?: boolean;
        auto_select?: boolean;
      }) => void;
      prompt: (listener?: (notification: unknown) => void) => void;
      renderButton: (el: HTMLElement, options: Record<string, unknown>) => void;
      disableAutoSelect: () => void;
    };
    oauth2: {
      initTokenClient: (cfg: {
        client_id: string;
        scope: string;
        callback: (resp: { access_token?: string; error?: string }) => void;
      }) => { requestAccessToken: () => void };
    };
  };
} | undefined;

declare const FB: {
  init: (cfg: { appId: string; cookie?: boolean; xfbml?: boolean; version: string }) => void;
  login: (
    cb: (resp: {
      status: 'connected' | 'not_authorized' | 'unknown';
      authResponse: { accessToken: string; userID: string } | null;
    }) => void,
    opts?: { scope?: string }
  ) => void;
  api: <T>(path: string, params: Record<string, string>, cb: (resp: T) => void) => void;
} | undefined;

@Injectable({ providedIn: 'root' })
export class SocialAuthService {
  private googleReady: Promise<void> | null = null;
  private facebookReady: Promise<void> | null = null;

  /**
   * Triggers Google Identity Services. Returns a verified ID token + profile. The
   * SDK is lazy-loaded; we don't ship it on first paint.
   *
   * If Google isn't configured (client id placeholder) we throw a clear error so
   * the UI can show a "Google sign-in is not configured" message instead of a
   * broken popup. The same error surfaces in dev so operators see it early.
   */
  async signInWithGoogle(): Promise<SocialAuthResult> {
    await this.ensureGoogleSdk();
    if (typeof google === 'undefined' || !google?.accounts?.id) {
      throw new Error('Google Identity Services failed to load');
    }
    if (GOOGLE_CLIENT_ID.startsWith('__')) {
      throw new Error(
        'Google sign-in is not configured. Set GOOGLE_CLIENT_ID in social-auth.service.ts.'
      );
    }

    return new Promise<SocialAuthResult>((resolve, reject) => {
      try {
        google!.accounts.id.initialize({
          client_id: GOOGLE_CLIENT_ID,
          ux_mode: 'popup',
          cancel_on_tap_outside: false,
          auto_select: false,
          callback: (resp) => {
            if (!resp?.credential) {
              reject(new Error('No credential returned from Google'));
              return;
            }
            const profile = SocialAuthService.decodeGoogleIdToken(resp.credential);
            if (!profile?.email) {
              reject(new Error('Google profile is missing email'));
              return;
            }
            resolve({
              provider: 'GOOGLE',
              idToken: resp.credential,
              accessToken: null,
              email: profile.email,
              firstName: profile.givenName || '',
              lastName: profile.familyName || '',
              providerUserId: profile.sub || '',
            });
          },
        });
        // We can't render an actual button on every screen, so we trigger the one-tap prompt.
        google!.accounts.id.prompt();
      } catch (e) {
        reject(e instanceof Error ? e : new Error(String(e)));
      }
    });
  }

  async signInWithFacebook(): Promise<SocialAuthResult> {
    await this.ensureFacebookSdk();
    if (typeof FB === 'undefined' || !FB?.login) {
      throw new Error('Facebook SDK failed to load');
    }
    if (FACEBOOK_APP_ID.startsWith('__')) {
      throw new Error(
        'Facebook sign-in is not configured. Set FACEBOOK_APP_ID in social-auth.service.ts.'
      );
    }
    return new Promise<SocialAuthResult>((resolve, reject) => {
      FB!.login((resp) => {
        if (resp.status !== 'connected' || !resp.authResponse) {
          reject(new Error('Facebook login cancelled or denied'));
          return;
        }
        const at = resp.authResponse.accessToken;
        const userId = resp.authResponse.userID;
        FB!.api<{ email?: string; first_name?: string; last_name?: string; id?: string }>(
          '/me',
          { fields: 'email,first_name,last_name,id' },
          (profile) => {
            if (!profile?.email) {
              reject(new Error('Facebook profile is missing email'));
              return;
            }
            resolve({
              provider: 'FACEBOOK',
              idToken: null,
              accessToken: at,
              email: profile.email,
              firstName: profile.first_name || '',
              lastName: profile.last_name || '',
              providerUserId: profile.id || userId,
            });
          }
        );
      }, { scope: 'public_profile,email' });
    });
  }

  /** Apple sign-in placeholder. Wire up AppleID JS + backend JWKS before enabling. */
  signInWithApple(): Promise<SocialAuthResult> {
    return Promise.reject(new Error('Apple sign-in is not configured yet.'));
  }

  private ensureGoogleSdk(): Promise<void> {
    if (this.googleReady) return this.googleReady;
    this.googleReady = new Promise<void>((resolve, reject) => {
      if (typeof document === 'undefined') {
        reject(new Error('No document — cannot load Google SDK'));
        return;
      }
      if (typeof google !== 'undefined' && google?.accounts?.id) {
        resolve();
        return;
      }
      const script = document.createElement('script');
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      script.onload = () => resolve();
      script.onerror = () => reject(new Error('Failed to load Google Identity Services'));
      document.head.appendChild(script);
    });
    return this.googleReady;
  }

  private ensureFacebookSdk(): Promise<void> {
    if (this.facebookReady) return this.facebookReady;
    this.facebookReady = new Promise<void>((resolve, reject) => {
      if (typeof document === 'undefined') {
        reject(new Error('No document — cannot load Facebook SDK'));
        return;
      }
      if (typeof FB !== 'undefined' && typeof FB.login === 'function') {
        resolve();
        return;
      }
      const w = window as unknown as { fbAsyncInit?: () => void };
      w.fbAsyncInit = () => {
        try {
          FB!.init({ appId: FACEBOOK_APP_ID, cookie: true, xfbml: false, version: 'v18.0' });
          resolve();
        } catch (e) {
          reject(e instanceof Error ? e : new Error(String(e)));
        }
      };
      const script = document.createElement('script');
      script.src = 'https://connect.facebook.net/en_US/sdk.js';
      script.async = true;
      script.defer = true;
      script.crossOrigin = 'anonymous';
      script.onerror = () => reject(new Error('Failed to load Facebook SDK'));
      document.head.appendChild(script);
    });
    return this.facebookReady;
  }

  /**
   * Decodes a Google ID token (JWT) so the UI can read the email / name without a
   * round-trip. This is NOT a security check — the backend re-verifies the token
   * against Google's tokeninfo endpoint before issuing a Bizly session.
   *
   * Returned object uses camelCase keys so consumers can use regular property
   * access (the raw JWT payload uses snake_case which trips
   * {@code noPropertyAccessFromIndexSignature}).
   */
  private static decodeGoogleIdToken(idToken: string): GoogleIdTokenProfile | null {
    try {
      const parts = idToken.split('.');
      if (parts.length !== 3) return null;
      const json = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
      const raw = JSON.parse(json) as Record<string, unknown>;
      return {
        email: typeof raw['email'] === 'string' ? (raw['email'] as string) : '',
        givenName: typeof raw['given_name'] === 'string' ? (raw['given_name'] as string) : '',
        familyName: typeof raw['family_name'] === 'string' ? (raw['family_name'] as string) : '',
        sub: typeof raw['sub'] === 'string' ? (raw['sub'] as string) : '',
      };
    } catch {
      return null;
    }
  }
}

interface GoogleIdTokenProfile {
  email: string;
  givenName: string;
  familyName: string;
  sub: string;
}
