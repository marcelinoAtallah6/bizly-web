import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

/**
 * Verified profile snapshot returned by Google. Used to authenticate via
 * {@link AuthService#socialLogin}.
 */
export interface SocialAuthResult {
  provider: 'GOOGLE';
  idToken?: string | null;
  accessToken?: string | null;
  email: string;
  firstName: string;
  lastName: string;
  providerUserId: string;
}

type GoogleCredentialCallback = (resp: { credential: string }) => void;

interface GooglePromptNotification {
  isNotDisplayed?: () => boolean;
  isSkippedMoment?: () => boolean;
  isDismissedMoment?: () => boolean;
  getNotDisplayedReason?: () => string;
  getSkippedReason?: () => string;
  getDismissedReason?: () => string;
}

declare const google: {
  accounts: {
    id: {
      initialize: (cfg: {
        client_id: string;
        callback: GoogleCredentialCallback;
        ux_mode?: 'popup' | 'redirect';
        cancel_on_tap_outside?: boolean;
        auto_select?: boolean;
      }) => void;
      prompt: (listener?: (notification: GooglePromptNotification) => void) => void;
      renderButton: (
        parent: HTMLElement,
        options: Record<string, string | number | boolean>
      ) => void;
      cancel: () => void;
    };
  };
} | undefined;

const GOOGLE_SIGNIN_TIMEOUT_MS = 120_000;

@Injectable({ providedIn: 'root' })
export class SocialAuthService {
  private googleReady: Promise<void> | null = null;

  /**
   * Opens Google's account picker (popup via official button). Avoids One Tap {@code prompt()}
   * hanging when the user clicked a custom CTA and GIS chose not to show the moment.
   */
  async signInWithGoogle(): Promise<SocialAuthResult> {
    await this.ensureGoogleSdk();
    if (typeof google === 'undefined' || !google?.accounts?.id) {
      throw new Error('Google Identity Services failed to load');
    }
    const googleClientId = (environment.googleWebClientId || '').trim();
    if (!googleClientId || !googleClientId.endsWith('.apps.googleusercontent.com')) {
      throw new Error(
        'Google sign-in is not configured. Set environment.googleWebClientId in src/environments/environment.ts.'
      );
    }
    return this.signInWithGooglePicker(googleClientId);
  }

  private signInWithGooglePicker(googleClientId: string): Promise<SocialAuthResult> {
    return new Promise<SocialAuthResult>((resolve, reject) => {
      let settled = false;
      const overlay = document.createElement('div');
      overlay.className = 'bizly-google-signin-overlay';
      overlay.setAttribute('role', 'dialog');
      overlay.setAttribute('aria-label', 'Sign in with Google');

      const panel = document.createElement('div');
      panel.className = 'bizly-google-signin-panel';

      const title = document.createElement('p');
      title.className = 'bizly-google-signin-title';
      title.textContent = 'Continue with Google';

      const buttonHost = document.createElement('div');
      buttonHost.className = 'bizly-google-signin-button-host';

      const cancel = document.createElement('button');
      cancel.type = 'button';
      cancel.className = 'bizly-google-signin-cancel';
      cancel.textContent = 'Cancel';

      panel.append(title, buttonHost, cancel);
      overlay.appendChild(panel);
      document.body.appendChild(overlay);

      const cleanup = () => {
        try {
          google?.accounts?.id?.cancel?.();
        } catch {
          /* ignore */
        }
        overlay.remove();
      };

      const finish = (fn: () => void) => {
        if (settled) return;
        settled = true;
        cleanup();
        fn();
      };

      const timer = window.setTimeout(
        () => finish(() => reject(new Error('Google sign-in timed out. Please try again.'))),
        GOOGLE_SIGNIN_TIMEOUT_MS
      );

      cancel.addEventListener('click', () => {
        window.clearTimeout(timer);
        finish(() => reject(new Error('Google sign-in was cancelled.')));
      });

      overlay.addEventListener('click', (ev) => {
        if (ev.target === overlay) {
          window.clearTimeout(timer);
          finish(() => reject(new Error('Google sign-in was cancelled.')));
        }
      });

      try {
        google!.accounts.id.initialize({
          client_id: googleClientId,
          ux_mode: 'popup',
          auto_select: false,
          cancel_on_tap_outside: true,
          callback: (resp) => {
            window.clearTimeout(timer);
            if (!resp?.credential) {
              finish(() => reject(new Error('No credential returned from Google')));
              return;
            }
            try {
              const profile = SocialAuthService.decodeJwtPayload(resp.credential);
              if (!profile?.email) {
                finish(() => reject(new Error('Google profile is missing email')));
                return;
              }
              finish(() =>
                resolve({
                  provider: 'GOOGLE',
                  idToken: resp.credential,
                  accessToken: null,
                  email: profile.email,
                  firstName: profile.givenName || '',
                  lastName: profile.familyName || '',
                  providerUserId: profile.sub || '',
                })
              );
            } catch (e) {
              finish(() =>
                reject(e instanceof Error ? e : new Error('Could not read Google profile'))
              );
            }
          },
        });

        google!.accounts.id.renderButton(buttonHost, {
          type: 'standard',
          theme: 'outline',
          size: 'large',
          text: 'continue_with',
          shape: 'rectangular',
          width: 280,
        });

        window.setTimeout(() => {
          const gBtn =
            buttonHost.querySelector<HTMLElement>('div[role="button"]') ??
            buttonHost.querySelector<HTMLElement>('iframe');
          gBtn?.click();
        }, 120);
      } catch (e) {
        window.clearTimeout(timer);
        finish(() => reject(e instanceof Error ? e : new Error(String(e))));
      }
    });
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

  private static decodeJwtPayload(token: string): GoogleJwtProfile | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const json = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
      const raw = JSON.parse(json) as Record<string, unknown>;
      return {
        email: typeof raw['email'] === 'string' ? raw['email'] : '',
        givenName: typeof raw['given_name'] === 'string' ? raw['given_name'] : '',
        familyName: typeof raw['family_name'] === 'string' ? raw['family_name'] : '',
        sub: typeof raw['sub'] === 'string' ? raw['sub'] : '',
      };
    } catch {
      return null;
    }
  }
}

interface GoogleJwtProfile {
  email: string;
  givenName: string;
  familyName: string;
  sub: string;
}
