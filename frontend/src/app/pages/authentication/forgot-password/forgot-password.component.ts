import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatStepper } from '@angular/material/stepper';
import { finalize } from 'rxjs';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss'],
})
export class AppForgotPasswordComponent implements OnInit, AfterViewInit {
  @ViewChild('stepper') stepper?: MatStepper;
  stepperLinear = true;
  private pendingQueryTokenVerification = false;

  currentStep = 0;

  requestLoading = false;
  verifyLoading = false;
  resetLoading = false;

  requestError = '';
  requestSuccess = '';
  verifyError = '';
  resetError = '';
  resetSuccess = '';

  requestForm = this.formBuilder.group({
    username: ['', [Validators.required, Validators.pattern('^[a-zA-Z0-9]+$')]],
  });

  verifyForm = this.formBuilder.group({
    token: ['', [Validators.required]],
  });

  resetForm = this.formBuilder.group({
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]],
  });

  tokenFromQuery = false;

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (token) {
      this.verifyForm.controls.token.setValue(token);
      this.currentStep = 1;
      this.tokenFromQuery = true;
      this.stepperLinear = false;
      this.pendingQueryTokenVerification = true;
    }
  }

  ngAfterViewInit(): void {
    if (this.pendingQueryTokenVerification) {
      this.pendingQueryTokenVerification = false;
      this.onVerifyToken(true);
    }
  }

  onSendResetLink(): void {
    this.requestError = '';
    this.requestSuccess = '';

    if (this.requestForm.invalid) {
      this.requestForm.markAllAsTouched();
      return;
    }

    const username = this.requestForm.controls.username.value ?? '';

    this.requestLoading = true;
    this.authService
      .forgotPassword(username)
      .pipe(finalize(() => (this.requestLoading = false)))
      .subscribe({
        next: (response) => {
          this.requestSuccess =
            response?.message ?? 'If your username exists, a reset link was sent.';
          this.currentStep = 1;
        },
        error: (error) => {
          this.requestError =
            error?.error?.message ?? 'Could not send reset instructions.';
        },
      });
  }

  onVerifyToken(autoVerify = false): void {
    this.verifyError = '';

    if (this.verifyForm.invalid) {
      this.verifyForm.markAllAsTouched();
      if (autoVerify) {
        this.tokenFromQuery = false;
      }
      return;
    }

    const token = this.verifyForm.controls.token.value ?? '';

    this.verifyLoading = true;
    this.authService
      .verifyForgotPasswordToken(token)
      .pipe(finalize(() => {
        this.verifyLoading = false;
      }))
      .subscribe({
        next: () => {
          this.currentStep = 2;
          if (this.stepper) {
            this.stepper.next();
          }
        },
        error: (error) => {
          if (autoVerify) {
            this.tokenFromQuery = false;
            this.stepperLinear = true;
          }
          this.verifyError = error?.error?.message ?? 'Invalid or expired token.';
        },
      });
  }

  onResetPassword(): void {
    this.resetError = '';
    this.resetSuccess = '';

    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }

    const newPassword = this.resetForm.controls.newPassword.value ?? '';
    const confirmPassword = this.resetForm.controls.confirmPassword.value ?? '';

    if (newPassword !== confirmPassword) {
      this.resetError = 'Password and confirmation must match.';
      return;
    }
    
      const passwordRegex =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,}$/;

  if (!passwordRegex.test(newPassword)) {
    this.resetError =
      'Password must be at least 8 characters and include uppercase, lowercase, number, and special character.';
    return;
  }
    const token = this.verifyForm.controls.token.value ?? '';

    this.resetLoading = true;
    this.authService
      .resetForgotPassword(token,newPassword,confirmPassword)
      .pipe(finalize(() => (this.resetLoading = false)))
      .subscribe({
        next: (response) => {
          this.resetSuccess = response?.message ?? 'Password updated successfully.';
          setTimeout(() => this.router.navigate(['/authentication/login']), 1200);
        },
        error: (error) => {
          this.resetError = error?.error?.message ?? 'Failed to reset password.';
        },
      });
  }
}
