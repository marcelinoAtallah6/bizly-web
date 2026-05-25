import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-verify-email',
  templateUrl: './verify-email.component.html',
  styleUrls: ['./verify-email.component.scss'],
})
export class VerifyEmailComponent implements OnInit {
  errorMessage = '';
  successMessage = '';
  isSubmitting = false;
  token = '';
  hidePassword = true;
  hideConfirm = true;

  form = this.fb.group({
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token') ?? '';
    if (!this.token) {
      this.errorMessage = 'Invalid or missing verification link.';
    }
  }

  submit(): void {
    this.errorMessage = '';
    this.successMessage = '';
    if (this.form.invalid || !this.token) {
      this.form.markAllAsTouched();
      return;
    }
    if (this.form.value.password !== this.form.value.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }
    this.isSubmitting = true;
    this.authService
      .verifyEmailSetPassword(this.token, this.form.value.password ?? '')
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: () => {
          this.successMessage = 'Your password is set. You can sign in now.';
          setTimeout(() => void this.router.navigateByUrl('/authentication/login'), 2000);
        },
        error: (err) => {
          this.errorMessage =
            err?.error?.message ?? 'Could not verify email. The link may have expired.';
        },
      });
  }
}
