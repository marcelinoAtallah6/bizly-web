import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { finalize, firstValueFrom } from 'rxjs';
import { AuthService, BusinessTypeOption } from 'src/app/services/auth.service';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { UserProfileService } from 'src/app/services/user-profile.service';
import { UmWorkflowService } from 'src/app/pages/um/services/um-workflow.service';
import { BusinessRegistrationPendingDialogComponent } from './business-registration-pending-dialog.component';

/** Decorative icon map for the business-type tiles. See register.component.ts for the same map. */
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
 * Business-registration screen shown to authenticated users that don't have a
 * business yet (typical flow after a social sign-up). The user picks a
 * business-type role from the public catalog — that same role becomes their
 * primary role inside the new business. The backend re-validates the chosen
 * row before assignment, so an attacker cannot pick SUPER_ADMIN here.
 */
@Component({
  selector: 'app-register-business',
  templateUrl: './register-business.component.html',
  styleUrls: ['./register-business.component.scss'],
})
export class RegisterBusinessComponent implements OnInit {
  businessTypes: BusinessTypeOption[] = [];
  isLoadingTypes = false;
  typesError = '';

  form = this.formBuilder.group({
    businessName: ['', [Validators.required, Validators.maxLength(200)]],
    roleId: [null as number | null, [Validators.required]],
  });

  isSubmitting = false;
  errorMessage = '';

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly menuPerm: MenuPermissionService,
    private readonly router: Router,
    private readonly dialog: MatDialog,
    private readonly umWorkflow: UmWorkflowService,
    private readonly userProfile: UserProfileService
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

  typeLabel(t: BusinessTypeOption): string {
    return (t.label && t.label.trim()) || t.name;
  }

  typeIcon(t: BusinessTypeOption): string {
    return TYPE_ICON_MAP[t.name?.toUpperCase()] ?? 'sparkles';
  }

  selectType(roleId: number): void {
    this.form.controls.roleId.setValue(roleId);
    this.form.controls.roleId.markAsTouched();
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
     * The chosen business-type role id IS the user's starting role. The backend
     * re-validates the row (is_business_type=1, BUSINESS-level, non-system) so
     * a tampered client cannot promote itself by changing the id.
     */
    const body = {
      businessName: this.form.controls.businessName.value ?? '',
      roleId: this.form.controls.roleId.value,
    };

    this.authService
      .registerBusiness(body)
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
        next: (resp) => {
          const d = resp?.data;
          const landing = this.menuPerm.landingRoute();
          const businessName = d?.businessName ?? (this.form.controls.businessName.value ?? '');
          const needsApproval = d?.requiresBusinessApprovalWorkflow === true;
          if (needsApproval && d?.businessId != null && businessName) {
            this.umWorkflow
              .requestBusinessRegistration({
                businessId: d.businessId,
                businessName,
              })
              .subscribe({
                next: () => {
                  this.userProfile.refresh(true);
                  void firstValueFrom(this.authService.fetchMe())
                    .catch(() => null)
                    .finally(() => {
                      this.dialog
                        .open(BusinessRegistrationPendingDialogComponent, { width: '420px' })
                        .afterClosed()
                        .subscribe(() =>
                          this.router.navigateByUrl('/authentication/pending-business-approval')
                        );
                    });
                },
                error: (err: unknown) => {
                  const msg = (err as { error?: { message?: string } })?.error?.message;
                  this.errorMessage =
                    msg?.trim() ||
                    'Your business was created, but the approval request could not be started. Contact support or try again from your profile.';
                },
              });
            return;
          }
          this.router.navigateByUrl(landing);
        },
        error: (err) => {
          this.errorMessage =
            err?.error?.message ?? 'Could not register your business. Please try again.';
        },
      });
  }
}
