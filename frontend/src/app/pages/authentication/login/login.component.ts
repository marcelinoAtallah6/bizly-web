import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, firstValueFrom } from 'rxjs';
import { AuthService } from 'src/app/services/auth.service';
import { SocialAuthService } from 'src/app/services/social-auth.service';
import { UserProfileService } from 'src/app/services/user-profile.service';
import { MenuCatalogService } from 'src/app/services/menu-catalog.service';
import { UserFavoritesService } from 'src/app/services/user-favorites.service';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class AppSideLoginComponent {
  isLoading = false;
  errorMessage = '';
  hidePassword = true;

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
    private readonly menuPerm: MenuPermissionService,
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
          this.userProfile.refresh(true);
          void this.routeAfterLogin();
        },
        error: (error) => {
          this.errorMessage =
            error?.error?.message ?? 'Login failed. Please check your credentials.';
        },
      });
  }

  /**
   * Google sign-in. The provider returns a verified ID token, which we POST to
   * {@code /auth/social/google}; the backend re-verifies it before issuing a Bizly JWT.
   * New users without a business are routed to register-business.
   */
  async signInWithGoogle(): Promise<void> {
    this.errorMessage = '';
    this.isLoading = true;
    try {
      const result = await this.socialAuth.signInWithGoogle();
      const rememberDevice = this.loginForm.controls.rememberDevice.value ?? true;
      this.authService
        .socialLogin('google', result.idToken ?? null, result.accessToken ?? null, rememberDevice)
        .pipe(finalize(() => (this.isLoading = false)))
        .subscribe({
          next: () => {
            this.userProfile.refresh(true);
            void this.routeAfterLogin();
          },
          error: (error) => {
            this.errorMessage =
              error?.error?.message ?? 'Sign-in with Google failed.';
          },
        });
    } catch (e) {
      this.isLoading = false;
      this.errorMessage = e instanceof Error ? e.message : 'Sign-in with Google failed.';
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

    if (this.authService.getCachedPendingBusinessApproval()) {
      this.router.navigateByUrl('/authentication/pending-business-approval');
      return;
    }

    if (firstLogin) {
      this.router.navigateByUrl('/authentication/welcome');
      return;
    }
    if ((businessId == null) && !(roleLevel && roleLevel.toUpperCase() === 'ADMIN')) {
      this.router.navigateByUrl('/authentication/register-business');
      return;
    }

    /* Default landing comes from {@link MenuPermissionService#landingRoute} — admin → /dashboard,
     * BUSINESS with permissions → first accessible screen, BUSINESS with empty matrix →
     * /no-access. A pinned favorite still wins when it points to a screen the role can reach. */
    let target = this.menuPerm.landingRoute();
    try {
      const defaultRoute = await this.favorites.fetchDefaultRoute();
      if (defaultRoute) {
        await this.menuCatalog.ensureLoaded();
        if (this.menuCatalog.findByRoute(defaultRoute)) {
          target = defaultRoute;
        }
      }
    } catch (err) {
      console.warn('[LOGIN] failed to resolve default screen, falling back to landingRoute()', err);
    }
    this.router.navigateByUrl(target);
  }
}
