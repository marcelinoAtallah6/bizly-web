import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin, of, switchMap, tap } from 'rxjs';
import { RoleLevelResponse, ParentRoleOptionResponse } from 'src/app/core/models/um.models';
import { AuthService } from 'src/app/services/auth.service';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { isOwnActiveTeamRole } from '../../um-role.util';
import { UmRoleService } from '../../services/um-role.service';
import { UmTeamRoleService } from '../../services/um-team-role.service';

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
  roleLevels: RoleLevelResponse[] = [];
  parentOptions: ParentRoleOptionResponse[] = [];
  private teamRoles: { id: number; name: string }[] = [];

  get isBusinessTenant(): boolean {
    return this.auth.isBusinessTenant();
  }

  get pageTitle(): string {
    if (this.isBusinessTenant) {
      return this.mode === 'create' ? 'New team role' : 'Team role';
    }
    return this.mode === 'create' ? 'New role' : 'Edit role';
  }

  get formToolbar(): ToolbarButton[] {
    return [{ id: 'back', icon: 'arrow_back', tooltip: 'Back to list', action: () => this.cancel() }];
  }

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly umRoleService: UmRoleService,
    private readonly umTeamRoleService: UmTeamRoleService,
    private readonly auth: AuthService,
    private readonly menuPerm: MenuPermissionService
  ) {}

  ngOnInit(): void {
    this.mode = (this.route.snapshot.data['mode'] as 'create' | 'edit') ?? 'create';
    const idParam = this.route.snapshot.paramMap.get('id');
    this.roleId = idParam ? Number(idParam) : null;

    if (this.mode === 'create' && !this.menuPerm.can('/um/role', 'add')) {
      this.router.navigate(['/um', 'role']);
      return;
    }
    if (this.mode === 'edit' && !this.menuPerm.can('/um/role', 'edit')) {
      this.router.navigate(['/um', 'role']);
      return;
    }

    if (this.isBusinessTenant) {
      this.form = this.fb.group({
        name: ['', [Validators.required, Validators.maxLength(120)]],
        parentRoleId: [null as number | null],
      });
      if (this.mode === 'create') {
        this.form.get('parentRoleId')?.setValidators([Validators.required]);
      }
      this.loading = true;
      const teamList$ = this.umTeamRoleService.list();
      const parentOpts$ = this.mode === 'create' ? this.umTeamRoleService.parentOptions() : of([]);
      const role$ =
        this.mode === 'edit' && this.roleId != null && !Number.isNaN(this.roleId)
          ? this.umRoleService.get({ id: this.roleId })
          : of(null);
      forkJoin({ teamList: teamList$, parentOpts: parentOpts$, role: role$ })
        .pipe(finalize(() => (this.loading = false)))
        .subscribe({
          next: ({ teamList, parentOpts, role }) => {
            this.teamRoles = (teamList ?? []).map((r) => ({ id: r.id, name: r.name }));
            if (this.mode === 'create') {
              this.parentOptions = parentOpts ?? [];
              const template = this.parentOptions.find((o) => o.optionKind === 'TEMPLATE');
              if (template?.id != null) {
                this.form.patchValue({ parentRoleId: template.id });
              }
            } else if (role) {
              if (isOwnActiveTeamRole(this.auth, this.teamRoles, role.id)) {
                this.router.navigate(['/um', 'role', role.id]);
                return;
              }
              this.form.patchValue({ name: role.name ?? '' });
            }
          },
          error: () => {},
        });
      return;
    }

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      roleLevelId: [null as number | null, [Validators.required]],
      roleType: [null as number | null],
    });

    this.loading = true;
    const nextType$ = this.mode === 'create' ? this.umRoleService.nextRoleType() : of(null);
    forkJoin({
      levels: this.umRoleService.listRoleLevels(),
      nextType: nextType$,
    })
      .pipe(
        tap(({ levels, nextType }) => {
          this.roleLevels = levels ?? [];
          if (this.mode === 'create' && nextType != null && Number.isFinite(nextType.nextRoleType)) {
            this.form.patchValue({ roleType: nextType.nextRoleType });
          }
          this.form.get('roleType')?.disable({ emitEvent: false });
        }),
        switchMap(() => {
          if (this.mode === 'edit' && this.roleId != null && !Number.isNaN(this.roleId)) {
            return this.umRoleService.get({ id: this.roleId });
          }
          return of(null);
        }),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: (role) => {
          if (role) {
            this.form.patchValue({
              name: role.name ?? '',
              roleLevelId: role.roleLevelId ?? null,
              roleType: role.roleType ?? null,
            });
            this.form.get('roleLevelId')?.disable({ emitEvent: false });
            this.form.get('roleType')?.disable({ emitEvent: false });
          }
        },
        error: () => {},
      });
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

    this.saving = true;
    if (this.isBusinessTenant) {
      if (this.mode === 'create') {
        this.umTeamRoleService
          .add({
            name,
            parentRoleId: Number(v.parentRoleId),
          })
          .pipe(finalize(() => (this.saving = false)))
          .subscribe({
            next: (res) => this.router.navigate(['/um', 'role', res.id]),
            error: () => {},
          });
      } else if (this.roleId != null) {
        this.umRoleService
          .update({ id: this.roleId, name })
          .pipe(finalize(() => (this.saving = false)))
          .subscribe({
            next: () => this.router.navigate(['/um', 'role', this.roleId]),
            error: () => {},
          });
      }
      return;
    }

    const roleLevelId = Number(v.roleLevelId);
    if (!Number.isFinite(roleLevelId)) {
      this.form.get('roleLevelId')?.setErrors({ required: true });
      this.saving = false;
      return;
    }
    const payload: { name: string; roleLevelId: number } = {
      name,
      roleLevelId,
    };

    if (this.mode === 'create') {
      this.umRoleService
        .add(payload)
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: (res) => this.router.navigate(['/um', 'role', res.id]),
          error: () => {},
        });
    } else if (this.roleId != null) {
      const updateBody: { id: number; name: string } = {
        id: this.roleId,
        name,
      };
      this.umRoleService
        .update(updateBody)
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: () => this.router.navigate(['/um', 'role', this.roleId]),
          error: () => {},
        });
    }
  }
}
