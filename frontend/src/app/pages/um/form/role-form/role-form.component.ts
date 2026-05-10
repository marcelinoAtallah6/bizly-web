import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UM_SCREEN_ROUTES } from 'src/app/common/GlobalConstants';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { UmRoleService } from '../../services/um-role.service';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-role-form',
  templateUrl: './role-form.component.html',
  styleUrl: './role-form.component.scss',
})
export class RoleFormComponent implements OnInit {
  form!: FormGroup;
  mode: 'create' | 'edit' = 'create';
  roleId: number | null = null;
  loading = false;
  saving = false;

  get formToolbar(): ToolbarButton[] {
    return [{ id: 'back', icon: 'arrow_back', tooltip: 'Back to list', action: () => this.cancel() }];
  }

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly umRoleService: UmRoleService,
    private readonly menuPerm: MenuPermissionService
  ) {}

  ngOnInit(): void {
    this.mode = (this.route.snapshot.data['mode'] as 'create' | 'edit') ?? 'create';
    const idParam = this.route.snapshot.paramMap.get('id');
    this.roleId = idParam ? Number(idParam) : null;

    if (this.mode === 'create' && !this.menuPerm.can(UM_SCREEN_ROUTES.roles, 'add')) {
      this.router.navigate(['/um', 'role']);
      return;
    }
    if (this.mode === 'edit' && !this.menuPerm.can(UM_SCREEN_ROUTES.roles, 'edit')) {
      this.router.navigate(['/um', 'role']);
      return;
    }

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      roleType: [null as number | null, [Validators.required]],
    });

    if (this.mode === 'edit' && this.roleId != null && !Number.isNaN(this.roleId)) {
      this.loading = true;
      this.umRoleService
        .get({ id: this.roleId })
        .pipe(finalize(() => (this.loading = false)))
        .subscribe({
          next: (role) => {
            this.form.patchValue({
              name: role.name ?? '',
              roleType: role.roleType ?? null,
            });
          },
          error: () => {},
        });
    }
  }

  cancel(): void {
    this.router.navigate(['/um', 'role']);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const name = (v.name as string).trim();
    const roleType = Number(v.roleType);
    if (!Number.isFinite(roleType)) {
      this.form.get('roleType')?.setErrors({ invalid: true });
      return;
    }

    this.saving = true;
    if (this.mode === 'create') {
      this.umRoleService
        .add({ name, roleType })
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: (res) => this.router.navigate(['/um', 'role', res.id]),
          error: () => {},
        });
    } else if (this.roleId != null) {
      this.umRoleService
        .update({ id: this.roleId, name, roleType })
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: () => this.router.navigate(['/um', 'role', this.roleId]),
          error: () => {},
        });
    }
  }
}
