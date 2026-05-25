import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize, switchMap, tap } from 'rxjs';
import { compressProfileImage } from 'src/app/common/profile-image.util';
import { AuthService } from 'src/app/services/auth.service';
import { UserProfileService } from 'src/app/services/user-profile.service';
import { UmUserService } from '../../services/um-user.service';

@Component({
  selector: 'app-my-profile',
  templateUrl: './my-profile.component.html',
  styleUrls: ['./my-profile.component.scss'],
})
export class MyProfileComponent implements OnInit {
  loading = true;
  saving = false;
  profilePreviewUrl: string | null = null;
  private pendingImage: { mime: string; base64: string } | null = null;
  removePhoto = false;

  form = this.fb.group({
    firstName: ['', [Validators.required, Validators.maxLength(64)]],
    lastName: ['', [Validators.required, Validators.maxLength(64)]],
    phoneNumber: ['', [Validators.maxLength(40)]],
  });

  emailDisplay = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly umUser: UmUserService,
    private readonly userProfile: UserProfileService
  ) {}

  ngOnInit(): void {
    this.emailDisplay = this.auth.getAccessTokenClaims()?.email ?? '';
    this.loadProfileForm();
  }

  private loadProfileForm(silent = false): void {
    if (!silent) {
      this.loading = true;
    }
    this.auth
      .fetchMe()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (res) => {
          const d = res?.data;
          if (!d) return;
          this.form.patchValue({
            firstName: d.firstName ?? '',
            lastName: d.lastName ?? '',
            phoneNumber: d.mobileNumber ?? '',
          });
          if (d.profileImageMime && d.profileImageBase64) {
            this.profilePreviewUrl = `data:${d.profileImageMime};base64,${d.profileImageBase64}`;
          } else {
            const c = this.auth.getAccessTokenClaims();
            if (c?.profileImageBase64 && c?.profileImageMime) {
              this.profilePreviewUrl = `data:${c.profileImageMime};base64,${c.profileImageBase64}`;
            } else {
              this.profilePreviewUrl = null;
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
    compressProfileImage(file)
      .then(({ mime, base64 }) => {
        this.pendingImage = { mime, base64 };
        this.profilePreviewUrl = `data:${mime};base64,${base64}`;
        this.removePhoto = false;
      })
      .catch(() => {
        this.pendingImage = null;
        this.profilePreviewUrl = null;
      })
      .finally(() => {
        input.value = '';
      });
  }

  clearProfilePhoto(): void {
    this.pendingImage = null;
    this.profilePreviewUrl = null;
    this.removePhoto = true;
  }

  submit(): void {
    if (this.saving) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.saving = true;
    this.umUser
      .updateSelfProfile({
        firstName: (v.firstName ?? '').trim(),
        lastName: (v.lastName ?? '').trim(),
        phoneNumber: (v.phoneNumber ?? '').trim() || undefined,
        ...(this.removePhoto ? { clearProfileImage: true } : {}),
        ...(this.pendingImage
          ? {
              profileImageMimeType: this.pendingImage.mime,
              profileImageBase64: this.pendingImage.base64,
            }
          : {}),
      })
      .pipe(
        switchMap(() => this.auth.refreshToken()),
        switchMap(() => this.auth.fetchMe()),
        switchMap(() => this.userProfile.refreshAndWait(true)),
        finalize(() => (this.saving = false))
      )
      .subscribe({
        next: () => {
          this.pendingImage = null;
          this.removePhoto = false;
          this.loadProfileForm(true);
        },
        error: () => {},
      });
  }
}
