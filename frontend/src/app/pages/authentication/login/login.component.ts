import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, firstValueFrom } from 'rxjs';
import { AuthService } from 'src/app/services/auth.service';
import { SocialAuthService } from 'src/app/services/social-auth.service';
import { UserProfileService } from 'src/app/services/user-profile.service';
import { MenuCatalogService } from 'src/app/services/menu-catalog.service';
import { UserFavoritesService } from 'src/app/services/user-favorites.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
})
export class AppSideLoginComponent {
  isLoading = false;
  errorMessage = '';

  loginForm = this.formBuilder.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
    rememberDevice: [true],
  });

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly socialAuth: SocialAuthService,
    private readonly userProfile: UserProfileService,
    private readonly menuCatalog: MenuCatalogService,
    private readonly favorites: UserFavoritesService,
    private readonly router: Router
  ) {}

  onSubmit(): void {
    this.errorMessage = '';

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const username = this.loginForm.controls.username.value ?? '';
    const password = this.loginForm.controls.password.value ?? '';
    const rememberDevice = this.loginForm.controls.rememberDevice.value ?? true;

    this.isLoading = true;

    this.authService
      .login(username, password, rememberDevice)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: () => {
          this.userProfile.refresh();
          void this.routeAfterLogin();
        },
        error: (error) => {
          this.errorMessage =
            error?.error?.message ?? 'Login failed. Please check your credentials.';
        },
      });
  }

  /**
   * Social-login click handler. Drives the real Google Identity Services / Facebook JS SDK
   * popup (no token prompt). The provider returns a verified ID token / access token, which
   * we POST to {@code /auth/social/<provider>}; the backend re-verifies it with the provider
   * before issuing a Bizly JWT.
   *
   * Flow per provider:
   *   - Google   → ID token via Google One-Tap popup.
   *   - Facebook → access token via FB.login.
   *   - Apple    → not yet configured.
   *
   * If the email is unknown to Bizly, the backend auto-provisions the user with
   * {@code first_login = 1} and {@code business_id = null}; the {@code OnboardingGuard}
   * then funnels them to {@code /authentication/register-business} to finish setup.
   */
  async signInWithProvider(provider: 'google' | 'facebook' | 'apple'): Promise<void> {
    this.errorMessage = '';
    this.isLoading = true;
    try {
      let result;
      switch (provider) {
        case 'google':   result = await this.socialAuth.signInWithGoogle();   break;
        case 'facebook': result = await this.socialAuth.signInWithFacebook(); break;
        case 'apple':    result = await this.socialAuth.signInWithApple();   break;
      }
      const rememberDevice = this.loginForm.controls.rememberDevice.value ?? true;
      this.authService
        .socialLogin(provider, result.idToken ?? null, result.accessToken ?? null, rememberDevice)
        .pipe(finalize(() => (this.isLoading = false)))
        .subscribe({
          next: () => {
            this.userProfile.refresh();
            void this.routeAfterLogin();
          },
          error: (error) => {
            this.errorMessage =
              error?.error?.message ?? `Sign-in with ${provider} failed.`;
          },
        });
    } catch (e) {
      this.isLoading = false;
      this.errorMessage = e instanceof Error ? e.message : `Sign-in with ${provider} failed.`;
    }
  }

  /**
   * Picks the post-login landing screen.
   *
   * The flow:
   *   1. Hit /user-favorites/default-route/get — cheap, returns the user's
   *      pinned default or null.
   *   2. If null, go to /dashboard (existing behaviour).
   *   3. If a route is returned, verify it's still in the user's accessible
   *      menus (role might have changed since the favorite was created).
   *      If accessible → navigate there. Otherwise fall back to /dashboard
   *      and silently let the dropdown surface the stale favorite next time.
   */
  private async routeAfterLogin(): Promise<void> {
    /*
     * Onboarding-aware redirect:
     *   - First-time login → fullscreen welcome wizard.
     *   - Business not yet registered (and user is not an ADMIN) → registration screen.
     *   - Otherwise → user's default favorite route, falling back to /dashboard.
     * We hit /auth/me to get an authoritative snapshot in case localStorage is stale.
     */
    let firstLogin = this.authService.getCachedFirstLogin();
    let businessId = this.authService.getCachedBusinessId();
    let roleLevel = this.authService.getCachedRoleLevel();
    try {
      const me = await firstValueFrom(this.authService.fetchMe());
      const d = me?.data;
      if (d) {
        firstLogin = !!d.firstLogin;
        businessId = d.businessId ?? null;
        roleLevel = d.roleLevel ?? null;
      }
    } catch (err) {
      console.warn('[LOGIN] /auth/me failed; relying on cached session state', err);
    }

    if (firstLogin) {
      this.router.navigateByUrl('/authentication/welcome');
      return;
    }
    if ((businessId == null) && !(roleLevel && roleLevel.toUpperCase() === 'ADMIN')) {
      this.router.navigateByUrl('/authentication/register-business');
      return;
    }

    let target = '/dashboard';
    try {
      const defaultRoute = await this.favorites.fetchDefaultRoute();
      if (defaultRoute) {
        await this.menuCatalog.ensureLoaded();
        if (this.menuCatalog.findByRoute(defaultRoute)) {
          target = defaultRoute;
        }
      }
    } catch (err) {
      console.warn('[LOGIN] failed to resolve default screen, falling back to dashboard', err);
    }
    this.router.navigateByUrl(target);
  }
}
