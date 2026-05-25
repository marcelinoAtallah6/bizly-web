import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, firstValueFrom } from 'rxjs';

import { AuthService, BusinessTypeOption, RegisterRequest } from 'src/app/services/auth.service';
import { SocialAuthService } from 'src/app/services/social-auth.service';
import { UserProfileService } from 'src/app/services/user-profile.service';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';

/**
 * Visual decoration for the business-type picker — purely cosmetic, based on
 * the role name. The list of pickable types ALWAYS comes from the backend; this
 * map only colours the cards. Unknown roles fall back to the generic icon.
 */
const TYPE_ICON_MAP: Record<string, string> = {
  RESTAURANT: 'tools-kitchen-2',
  CLINIC: 'stethoscope',
  BEAUTY_CENTER: 'scissors',
  SALON: 'scissors',
  GYM: 'barbell',
  TAXI_COMPANY: 'car',
  RETAIL: 'building-store',
  SERVICES: 'briefcase',
  OTHER: 'sparkles',
};

/**
 * Public "Create Account" screen.
 *
 * <p>Two ways in:</p>
 * <ul>
 *   <li><b>Manual sign-up</b> — the 2-step wizard:
 *     <ol>
 *       <li>User info (username / email / first / last / password).</li>
 *       <li>Business info (name + type).</li>
 *     </ol>
 *     Submitted in one transactional call to {@code POST /auth/register}; the
 *     backend bootstraps the tenant and returns a JWT in the same response so
 *     the SPA lands on the dashboard with no second login round-trip.</li>
 *
 *   <li><b>Social sign-up</b> — "Sign up with Google / Facebook" buttons.
 *     These behave IDENTICALLY to the login screen's social buttons: the
 *     provider SDK returns a verified ID token, we POST it to
 *     {@code /auth/social/<provider>}, and the backend either logs the user
 *     in (existing account) or auto-provisions a fresh row with no role / no
 *     business and returns an "onboarding JWT". Either way the SPA persists
 *     the session and routes:
 *       - Existing user with a business → user's landing route (dashboard or
 *         the first menu their role can reach).
 *       - New user without a business → {@code /authentication/register-business}
 *         to pick a business type (= starting role).</li>
 * </ul>
 *
 * <p>Security: the role assigned to a manual sign-up is decided server-side from
 * the role id the user picked in step 2; the backend re-validates that the row
 * is {@code is_business_type=1} and BUSINESS-level so a tampered client cannot
 * promote itself to SUPER_ADMIN. The password is RSA-encrypted with the public
 * certificate before leaving the browser, the same way {@code /auth/login} does.
 * Social sign-ups never expose a password field; the backend stores a random
 * one so the local-login path can never authenticate them.</p>
 */
@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class AppSideRegisterComponent implements OnInit {
  /** Loaded from POST /auth/business-types on init. Empty until the request resolves. */
  businessTypes: BusinessTypeOption[] = [];
  isLoadingTypes = false;
  typesError = '';

  // Step 1 — account.
  accountForm = this.formBuilder.group({
    username:        ['', [Validators.required, Validators.minLength(4), Validators.maxLength(64)]],
    email:           ['', [Validators.required, Validators.email]],
    firstName:       ['', [Validators.required, Validators.maxLength(64)]],
    lastName:        ['', [Validators.required, Validators.maxLength(64)]],
    mobileNumber:    [''],
    password:        ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]],
    acceptTerms:     [false, [Validators.requiredTrue]],
  });

  // Step 2 — business.
  // roleId holds the chosen business-type role id (numeric). The submit handler
  // sends it as-is; the server re-validates that the row is a non-system
  // BUSINESS-level role flagged is_business_type=1.
  businessForm = this.formBuilder.group({
    businessName: ['', [Validators.required, Validators.maxLength(200)]],
    roleId:       [null as number | null, [Validators.required]],
  });

  step: 1 | 2 = 1;
  isSubmitting = false;
  /** True while a social provider popup is open / a /auth/social request is in flight.
   *  Disables every CTA so the user cannot fire two parallel auth attempts. */
  isSocialBusy = false;
  errorMessage = '';

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly socialAuth: SocialAuthService,
    private readonly userProfile: UserProfileService,
    private readonly menuPerm: MenuPermissionService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadBusinessTypes();
  }

  private loadBusinessTypes(): void {
    this.isLoadingTypes = true;
    this.typesError = '';
    this.authService.listBusinessTypes().subscribe({
      next: (response) => {
        this.businessTypes = response?.data ?? [];
        this.isLoadingTypes = false;
      },
      error: () => {
        this.isLoadingTypes = false;
        this.typesError = 'Could not load business types. Please refresh and try again.';
      },
    });
  }

  /** Pretty label for a row in the picker. */
  typeLabel(t: BusinessTypeOption): string {
    return (t.label && t.label.trim()) || t.name;
  }

  /** Decorative icon for a picker tile — defaults to a sparkle for unknown rows. */
  typeIcon(t: BusinessTypeOption): string {
    return TYPE_ICON_MAP[t.name?.toUpperCase()] ?? 'sparkles';
  }

  // ---------------------------------------------------------------------------
  // Social sign-up = social sign-in.
  // ---------------------------------------------------------------------------
  /**
   * Click handler for the Google / Facebook buttons on the register screen.
   *
   * Mirrors the login screen's social flow exactly:
   *   1. Open the provider SDK (One-Tap for Google, FB.login for Facebook).
   *   2. POST the verified provider token to {@code /auth/social/<provider>}.
   *   3. Persist the returned Bizly JWT (handled inside {@code AuthService.socialLogin}).
   *   4. Route the user — existing accounts get their landing screen, brand-new
   *      ones land on {@code /authentication/register-business} to pick a
   *      business type (which also becomes their starting role).
   *
   * The button never falls back to "pre-fill the manual form" anymore — that flow
   * forced the user to fill the wizard twice (once with Google data, then again
   * after). The current behaviour matches the user-facing copy ("Sign up with
   * Google") and the expected Google/Facebook UX everywhere else.
   */
  async signUpWithGoogle(): Promise<void> {
    if (this.isSocialBusy || this.isSubmitting) return;
    this.errorMessage = '';
    this.isSocialBusy = true;
    try {
      const result = await this.socialAuth.signInWithGoogle();
      this.authService
        .socialLogin('google', result.idToken ?? null, result.accessToken ?? null, true)
        .pipe(finalize(() => (this.isSocialBusy = false)))
        .subscribe({
          next: () => {
            this.userProfile.refresh(true);
            void this.routeAfterSocial();
          },
          error: (err) => {
            this.errorMessage =
              err?.error?.message ?? 'Sign-up with Google failed. Please try again.';
          },
        });
    } catch (e) {
      this.isSocialBusy = false;
      this.errorMessage = e instanceof Error ? e.message : 'Sign-up with Google failed.';
    }
  }

  /**
   * Post-social-login routing. Identical to the login screen's helper so the SPA
   * behaves the same regardless of which entry point the user clicked. Order:
   *   - {@code firstLogin = true}        → welcome wizard (admin-issued password reset path).
   *     For a brand-new social account the backend sets {@code firstLogin = 0},
   *     so this branch only triggers if the same email already had a temp-password
   *     account waiting.
   *   - {@code businessId == null} AND role-level ≠ ADMIN → business registration
   *     step. This is the Google "new user" path the design calls out.
   *   - Otherwise → the role's landing route (dashboard for admins, first
   *     accessible screen for tenants).
   */
  private async routeAfterSocial(): Promise<void> {
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
      console.warn('[REGISTER][SOCIAL] /auth/me failed, falling back to cached session', err);
    }

    if (this.authService.getCachedPendingBusinessApproval()) {
      this.router.navigateByUrl('/authentication/pending-business-approval');
      return;
    }

    if (firstLogin) {
      this.router.navigateByUrl('/authentication/welcome');
      return;
    }
    if (businessId == null && !(roleLevel && roleLevel.toUpperCase() === 'ADMIN')) {
      this.router.navigateByUrl('/authentication/register-business');
      return;
    }
    this.router.navigateByUrl(this.menuPerm.landingRoute());
  }

  // ---------------------------------------------------------------------------
  // Stepper (manual sign-up only)
  // ---------------------------------------------------------------------------
  nextStep(): void {
    this.errorMessage = '';
    if (this.accountForm.controls.password.value !==
        this.accountForm.controls.confirmPassword.value) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }
    if (this.accountForm.invalid) {
      this.accountForm.markAllAsTouched();
      return;
    }
    this.step = 2;
  }

  prevStep(): void {
    this.step = 1;
    this.errorMessage = '';
  }

  selectType(roleId: number): void {
    this.businessForm.controls.roleId.setValue(roleId);
    this.businessForm.controls.roleId.markAsTouched();
  }

  // ---------------------------------------------------------------------------
  // Final submit — atomic.
  // ---------------------------------------------------------------------------
  submit(): void {
    if (this.isSubmitting) return;
    this.errorMessage = '';
    if (this.businessForm.invalid) {
      this.businessForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;

    // Manual sign-up only — social sign-ups never get this far; they auto-login
    // through /auth/social/<provider> and finish onboarding on /register-business.
    const body: RegisterRequest = {
      username:     this.accountForm.controls.username.value     ?? '',
      email:        this.accountForm.controls.email.value        ?? '',
      firstName:    this.accountForm.controls.firstName.value    ?? '',
      lastName:     this.accountForm.controls.lastName.value     ?? '',
      mobileNumber: this.accountForm.controls.mobileNumber.value ?? '',
      password:        this.accountForm.controls.password.value        ?? '',
      confirmPassword: this.accountForm.controls.confirmPassword.value ?? '',
      businessName: this.businessForm.controls.businessName.value ?? '',
      // The chosen business-type role id IS the user's starting role; the
      // backend re-validates the row before assignment.
      roleId: this.businessForm.controls.roleId.value,
      authProvider: null,
      providerUserId: null,
    };

    this.authService
      .register(body, true)
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: async () => {
          this.userProfile.refresh();
          try {
            await firstValueFrom(this.authService.fetchMe());
          } catch {
            /* fall through using JWT + cache from register response */
          }
          if (this.authService.getCachedPendingBusinessApproval()) {
            void this.router.navigateByUrl('/authentication/pending-business-approval');
            return;
          }
          void this.router.navigateByUrl(this.menuPerm.landingRoute());
        },
        error: (err) => {
          this.errorMessage =
            err?.error?.message ?? 'Could not create your account. Please try again.';
        },
      });
  }
}
