import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { GetUserResponse } from 'src/app/core/models/um.models';
import { AuthService } from 'src/app/services/auth.service';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { UserProfileService } from 'src/app/services/user-profile.service';
import { UmRoleService } from '../../services/um-role.service';
import { UmUserService } from '../../services/um-user.service';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { catchError, finalize, of, switchMap, take } from 'rxjs';
import { compressProfileImage } from 'src/app/common/profile-image.util';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.scss',
})
export class UserFormComponent implements OnInit {
  form!: FormGroup;
  mode: 'create' | 'edit' = 'create';
  userId: number | null = null;
  loading = false;
  saving = false;
  roles: { id: number; name: string }[] = [];

  /** Preview URL (existing image or newly picked file). */
  profilePreviewUrl: string | null = null;
  /** New image payload for API (raw Base64, no data URL prefix). */
  pendingProfileImage: { mime: string; base64: string } | null = null;
  removePhoto = false;

  private readonly mobilePattern = '^\\+?[0-9]{7,15}$';

  /** Matches UM / auth policy: lower, upper, digit, special from @$!%*?&, min 8. */
  private readonly passwordStrengthPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,}$/;

  readonly statusOptions = ['ACTIVE', 'INACTIVE', 'LOCKED'];

  get formToolbar(): ToolbarButton[] {
    return [{ id: 'back', icon: 'arrow_back', tooltip: 'Back to list', action: () => this.cancel() }];
  }

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly umUserService: UmUserService,
    private readonly umRoleService: UmRoleService,
    private readonly auth: AuthService,
    private readonly userProfile: UserProfileService,
    private readonly menuPerm: MenuPermissionService
  ) {}

  ngOnInit(): void {
    this.mode = (this.route.snapshot.data['mode'] as 'create' | 'edit') ?? 'create';
    const idParam = this.route.snapshot.paramMap.get('id');
    this.userId = idParam ? Number(idParam) : null;

    if (this.mode === 'create' && !this.menuPerm.can('/um/user', 'add')) {
      this.router.navigate(['/um', 'user']);
      return;
    }
    if (this.mode === 'edit' && !this.menuPerm.can('/um/user', 'edit')) {
      this.router.navigate(['/um', 'user']);
      return;
    }

    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.maxLength(50)]],
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      mobileNumber: ['', [Validators.required, Validators.pattern(this.mobilePattern)]],
      status: ['ACTIVE', [Validators.required, Validators.maxLength(50)]],
      roleIds: [[] as number[], [this.rolesRequired.bind(this)]],
      passwordPlain: [''],
      passwordConfirm: [''],
    });

    if (this.mode === 'create') {
      this.setPasswordValidators(true);
    }

    this.loading = true;
    this.umRoleService
      .gets({ pageNumber: 0, pageSize: 200 })
      .pipe(
        switchMap((page) => {
          this.roles = (page.items ?? []).map((r) => ({ id: r.id, name: r.name }));
          if (this.mode === 'edit' && this.userId != null && !Number.isNaN(this.userId)) {
            return this.umUserService.get({ id: this.userId });
          }
          return of(null as GetUserResponse | null);
        }),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (user) => {
          if (user) {
            this.form.patchValue({
              username: user.username ?? '',
              firstName: user.firstName ?? '',
              lastName: user.lastName ?? '',
              email: user.email ?? '',
              mobileNumber: user.mobileNumber ?? '',
              status: user.status ?? 'ACTIVE',
              roleIds: user.roleIds ?? [],
            });
            this.setPasswordValidators(false);
            if (user.profileImageBase64 && user.profileImageMimeType) {
              this.profilePreviewUrl = `data:${user.profileImageMimeType};base64,${user.profileImageBase64}`;
            }
          }
        },
        error: () => {},
      });
  }

  onProfileFileSelected(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !file.type.startsWith('image/')) {
      return;
    }
    /* Resize on the client before hitting the wire. UM caps profile uploads at 2 MB and rejects
       anything larger with "Profile image exceeds maximum size"; a downscaled 256-px JPEG fits
       that budget every time AND lands small enough that the JWT can carry the avatar inline,
       so the navbar renders immediately on the next refresh without a /auth/me/avatar fetch. */
    compressProfileImage(file)
      .then(({ mime, base64 }) => {
        this.pendingProfileImage = { mime, base64 };
        this.profilePreviewUrl = `data:${mime};base64,${base64}`;
        this.removePhoto = false;
      })
      .catch(() => {
        this.pendingProfileImage = null;
        this.profilePreviewUrl = null;
      })
      .finally(() => {
        input.value = '';
      });
  }

  clearProfilePhoto(): void {
    this.pendingProfileImage = null;
    this.profilePreviewUrl = null;
    this.removePhoto = true;
  }

  private setPasswordValidators(enabled: boolean): void {
    const pwd = this.form.get('passwordPlain');
    const conf = this.form.get('passwordConfirm');
    if (enabled) {
      pwd?.setValidators([Validators.required, Validators.pattern(this.passwordStrengthPattern)]);
      conf?.setValidators([Validators.required, this.matchPassword]);
    } else {
      pwd?.clearValidators();
      conf?.clearValidators();
      pwd?.setValue('');
      conf?.setValue('');
    }
    pwd?.updateValueAndValidity();
    conf?.updateValueAndValidity();
  }

  private matchPassword = (control: AbstractControl): ValidationErrors | null => {
    const p = this.form?.get('passwordPlain')?.value as string;
    const c = control.value as string;
    if (!p && !c) return null;
    return p === c ? null : { mismatch: true };
  };

  private rolesRequired(control: AbstractControl): ValidationErrors | null {
    const v = control.value as number[];
    return v?.length ? null : { roles: true };
  }

  cancel(): void {
    this.router.navigate(['/um', 'user']);
  }

  private maybeRefreshNavbarProfile(savedUserId: number): void {
    const c = this.auth.getAccessTokenClaims();
    if (c?.userId === savedUserId) {
      // Navbar reads only from the JWT — rotate the access token so claims pick up name / email / avatar.
      this.auth
        .refreshToken()
        .pipe(
          catchError(() => of(null)),
          switchMap(() => this.userProfile.refresh(true)),
          take(1)
        )
        .subscribe();
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const base = {
      username: v.username.trim(),
      firstName: v.firstName.trim(),
      lastName: v.lastName.trim(),
      email: v.email.trim(),
      mobileNumber: v.mobileNumber.trim(),
      status: v.status,
    };

    this.saving = true;
    if (this.mode === 'create') {
      this.umUserService
        .add({
          ...base,
          passwordPlain: v.passwordPlain as string,
          roleIds: v.roleIds as number[],
          ...(this.pendingProfileImage
            ? {
                profileImageMimeType: this.pendingProfileImage.mime,
                profileImageBase64: this.pendingProfileImage.base64,
              }
            : {}),
        })
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: (res) => {
            this.maybeRefreshNavbarProfile(res.id);
            this.router.navigate(['/um', 'user', res.id]);
          },
          error: () => {},
        });
    } else if (this.userId != null) {
      this.umUserService
        .update({
          ...base,
          id: this.userId,
          roleIds: v.roleIds as number[],
          ...(this.removePhoto ? { clearProfileImage: true } : {}),
          ...(!this.removePhoto && this.pendingProfileImage
            ? {
                profileImageMimeType: this.pendingProfileImage.mime,
                profileImageBase64: this.pendingProfileImage.base64,
              }
            : {}),
        })
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: () => {
            this.maybeRefreshNavbarProfile(this.userId!);
            this.router.navigate(['/um', 'user', this.userId]);
          },
          error: () => {},
        });
    }
  }
}
