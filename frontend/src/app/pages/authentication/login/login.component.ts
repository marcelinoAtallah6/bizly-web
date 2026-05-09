import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from 'src/app/services/auth.service';
import { UserProfileService } from 'src/app/services/user-profile.service';

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
    private readonly userProfile: UserProfileService,
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
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.errorMessage =
            error?.error?.message ?? 'Login failed. Please check your credentials.';
        },
      });
  }
}
