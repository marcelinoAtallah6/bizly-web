import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService, RegisterRequest } from 'src/app/services/auth.service';
import { SocialAuthService, SocialAuthResult } from 'src/app/services/social-auth.service';
import { UserProfileService } from 'src/app/services/user-profile.service';

interface BusinessTypeOption {
  code: string;
  label: string;
  icon: string;
}

/**
 * Public "Create Account" wizard. Two steps:
 *   1. User info  (manual: username/email/password — OR populated from a social
 *      provider when the user clicks Sign-up-with-Google/Facebook).
 *   2. Business info (name + type).
 *
 * The wizard submits BOTH steps in one transactional call to POST /auth/register
 * so a brand-new tenant is fully bootstrapped before the user is ever "logged in".
 * The backend issues a JWT session in the same response, so the SPA lands on the
 * dashboard immediately — no second login round-trip and no post-login onboarding
 * redirect.
 *
 * Security: the role assigned to the new user is decided server-side. The DTO has
 * no roleId / roleName field; any client-side tampering is ignored. The password
 * is encrypted with the public certificate before leaving the browser, exactly
 * the same way /auth/login does.
 */
@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss'],
})
export class AppSideRegisterComponent {
  readonly businessTypes: BusinessTypeOption[] = [
    { code: 'RETAIL',     label: 'Retail / Shop',           icon: 'building-store' },
    { code: 'RESTAURANT', label: 'Restaurant',              icon: 'tools-kitchen-2' },
    { code: 'SALON',      label: 'Salon / Beauty',          icon: 'scissors' },
    { code: 'GYM',        label: 'Gym / Fitness',           icon: 'barbell' },
    { code: 'SERVICES',   label: 'Professional services',   icon: 'briefcase' },
    { code: 'OTHER',      label: 'Other',                   icon: 'sparkles' },
  ];

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
  businessForm = this.formBuilder.group({
    businessName: ['', [Validators.required, Validators.maxLength(200)]],
    businessType: ['', [Validators.required]],
  });

  step: 1 | 2 = 1;
  isSubmitting = false;
  errorMessage = '';

  /** When non-null the user signed up via a social provider; password fields are
   *  hidden and the backend generates a random password automatically. */
  socialProfile: SocialAuthResult | null = null;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly socialAuth: SocialAuthService,
    private readonly userProfile: UserProfileService,
    private readonly router: Router
  ) {}

  // ---------------------------------------------------------------------------
  // Social pre-fill
  // ---------------------------------------------------------------------------
  async signUpWithProvider(provider: 'google' | 'facebook' | 'apple'): Promise<void> {
    this.errorMessage = '';
    try {
      let result: SocialAuthResult;
      switch (provider) {
        case 'google':   result = await this.socialAuth.signInWithGoogle();   break;
        case 'facebook': result = await this.socialAuth.signInWithFacebook(); break;
        case 'apple':    result = await this.socialAuth.signInWithApple();   break;
      }
      this.socialProfile = result;
      // Pre-fill the account form. Username defaults to the e-mail local-part — the
      // user can still edit it before submitting.
      const usernameGuess = (result.email.split('@')[0] || '').replace(/[^A-Za-z0-9._-]/g, '');
      this.accountForm.patchValue({
        username: usernameGuess,
        email: result.email,
        firstName: result.firstName,
        lastName: result.lastName,
      });
      // Password not needed for social.
      this.accountForm.controls.password.clearValidators();
      this.accountForm.controls.confirmPassword.clearValidators();
      this.accountForm.controls.password.updateValueAndValidity();
      this.accountForm.controls.confirmPassword.updateValueAndValidity();
    } catch (e) {
      this.errorMessage = e instanceof Error ? e.message : 'Social sign-in failed';
    }
  }

  // ---------------------------------------------------------------------------
  // Stepper
  // ---------------------------------------------------------------------------
  nextStep(): void {
    this.errorMessage = '';
    // Custom: password fields only validate when this is a manual sign-up.
    if (!this.socialProfile && this.accountForm.controls.password.value !==
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

  selectType(code: string): void {
    this.businessForm.controls.businessType.setValue(code);
    this.businessForm.controls.businessType.markAsTouched();
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

    const body: RegisterRequest = {
      username:     this.accountForm.controls.username.value     ?? '',
      email:        this.accountForm.controls.email.value        ?? '',
      firstName:    this.accountForm.controls.firstName.value    ?? '',
      lastName:     this.accountForm.controls.lastName.value     ?? '',
      mobileNumber: this.accountForm.controls.mobileNumber.value ?? '',
      password:        this.socialProfile ? undefined : (this.accountForm.controls.password.value        ?? ''),
      confirmPassword: this.socialProfile ? undefined : (this.accountForm.controls.confirmPassword.value ?? ''),
      businessName: this.businessForm.controls.businessName.value ?? '',
      businessType: this.businessForm.controls.businessType.value ?? '',
      authProvider: this.socialProfile?.provider ?? null,
      providerUserId: this.socialProfile?.providerUserId ?? null,
    };

    this.authService
      .register(body, true)
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: () => {
          // Refresh cached profile + go straight to the dashboard.
          this.userProfile.refresh();
          this.router.navigateByUrl('/dashboard');
        },
        error: (err) => {
          this.errorMessage =
            err?.error?.message ?? 'Could not create your account. Please try again.';
        },
      });
  }
}
