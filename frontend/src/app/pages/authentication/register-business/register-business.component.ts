import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AssignableRole, AuthService } from 'src/app/services/auth.service';

interface BusinessTypeOption {
  code: string;
  label: string;
  icon: string;
}

/**
 * Business-registration form shown after a user logs in for the first time
 * (and after the welcome wizard). Backend assigns the role automatically — the
 * UI never sends a roleId. The "Your role" panel reads /auth/roles/assignable
 * purely for transparency; it is read-only.
 */
@Component({
  selector: 'app-register-business',
  templateUrl: './register-business.component.html',
  styleUrls: ['./register-business.component.scss'],
})
export class RegisterBusinessComponent implements OnInit {
  readonly businessTypes: BusinessTypeOption[] = [
    { code: 'RETAIL', label: 'Retail / Shop', icon: 'building-store' },
    { code: 'RESTAURANT', label: 'Restaurant', icon: 'tools-kitchen-2' },
    { code: 'SALON', label: 'Salon / Beauty', icon: 'scissors' },
    { code: 'GYM', label: 'Gym / Fitness', icon: 'barbell' },
    { code: 'SERVICES', label: 'Professional services', icon: 'briefcase' },
    { code: 'OTHER', label: 'Other', icon: 'sparkles' },
  ];

  form = this.formBuilder.group({
    businessName: ['', [Validators.required, Validators.maxLength(200)]],
    businessType: ['', [Validators.required]],
  });

  isSubmitting = false;
  isLoadingRoles = false;
  errorMessage = '';
  assignedRole: AssignableRole | null = null;
  otherAssignableRoles: AssignableRole[] = [];

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadAssignableRoles();
  }

  private loadAssignableRoles(): void {
    this.isLoadingRoles = true;
    this.authService.listAssignableRoles().subscribe({
      next: (response) => {
        const roles = response?.data ?? [];
        this.assignedRole = roles.find((r) => r.isDefault) ?? roles[0] ?? null;
        this.otherAssignableRoles = roles.filter((r) => !r.isDefault);
        this.isLoadingRoles = false;
      },
      error: () => {
        // Non-fatal — registration can still proceed; the backend picks the role.
        this.isLoadingRoles = false;
      },
    });
  }

  selectType(code: string): void {
    this.form.controls.businessType.setValue(code);
    this.form.controls.businessType.markAsTouched();
  }

  submit(): void {
    if (this.isSubmitting) return;
    this.errorMessage = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    /*
     * Important: the request body intentionally contains ONLY the public fields. Any roleId or
     * status field a tampered client tries to send is dropped server-side because the request DTO
     * does not declare them.
     */
    const body = {
      businessName: this.form.controls.businessName.value ?? '',
      businessType: this.form.controls.businessType.value ?? '',
    };

    this.authService
      .registerBusiness(body)
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: () => {
          this.router.navigateByUrl('/dashboard');
        },
        error: (err) => {
          this.errorMessage =
            err?.error?.message ?? 'Could not register your business. Please try again.';
        },
      });
  }
}
