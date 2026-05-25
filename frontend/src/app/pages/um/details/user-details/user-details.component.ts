import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, finalize, map, switchMap } from 'rxjs/operators';
import { GetRoleResponse, GetUserResponse } from 'src/app/core/models/um.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { AuthService } from 'src/app/services/auth.service';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { SimpleConfirmDialogComponent } from 'src/app/shared/dialogs/simple-confirm-dialog.component';
import { UmRoleService } from '../../services/um-role.service';
import { UmTeamRoleService } from '../../services/um-team-role.service';
import { UmUserService } from '../../services/um-user.service';

@Component({
  selector: 'app-user-details',
  templateUrl: './user-details.component.html',
  styleUrl: './user-details.component.scss',
})
export class UserDetailsComponent implements OnInit {
  user: GetUserResponse | null = null;
  avatarUrl: string | null = null;
  /** Resolved names for {@link GetUserResponse.roleIds}. */
  roleLabels: string[] = [];
  loading = false;
  deleting = false;

  get detailToolbar(): ToolbarButton[] {
    const buttons: ToolbarButton[] = [
      { id: 'back', icon: 'arrow_back', tooltip: 'Back to list', action: () => this.back() },
    ];
    if (this.menuPerm.can('/um/user', 'delete')) {
      buttons.push({
        id: 'delete',
        icon: 'delete_outline',
        tooltip: 'Delete user',
        action: () => this.delete(),
        disabled: !this.user || this.deleting,
        color: 'warn',
      });
    }
    if (this.menuPerm.can('/um/user', 'edit')) {
      buttons.push({
        id: 'edit',
        icon: 'edit',
        tooltip: 'Edit user',
        action: () => this.edit(),
        disabled: !this.user,
        color: 'primary',
      });
    }
    return buttons;
  }

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly umUserService: UmUserService,
    private readonly umRoleService: UmRoleService,
    private readonly umTeamRoleService: UmTeamRoleService,
    private readonly auth: AuthService,
    private readonly dialog: MatDialog,
    private readonly menuPerm: MenuPermissionService
  ) {}

  /** Team-role API is for business tenants only — not portal admins (even with business context). */
  private get useTeamRoles(): boolean {
    return this.auth.isBusinessTenant() && !this.auth.isSystemAdmin();
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id)) {
      this.router.navigate(['/um', 'user']);
      return;
    }
    this.load(id);
  }

  load(id: number): void {
    this.loading = true;
    this.umUserService
      .get({ id })
      .pipe(
        switchMap((user) =>
          this.resolveRoleLabels(user).pipe(map((roleLabels) => ({ user, roleLabels })))
        ),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: ({ user, roleLabels }) => {
          this.user = user;
          this.roleLabels = roleLabels;
          if (user.profileImageBase64 && user.profileImageMimeType) {
            this.avatarUrl = `data:${user.profileImageMimeType};base64,${user.profileImageBase64}`;
          } else {
            this.avatarUrl = null;
          }
        },
        error: () => {},
      });
  }

  private loadRoleNameMap(): Observable<Map<number, string>> {
    if (this.auth.getAdminBusinessContext() != null && !this.auth.isBusinessTenant()) {
      return this.umRoleService.gets({ pageNumber: 0, pageSize: 500, globalTemplatesOnly: true }).pipe(
        map((page) => new Map((page.items ?? []).map((r) => [r.id, r.name])))
      );
    }
    if (this.useTeamRoles) {
      return this.umTeamRoleService.list().pipe(
        map((items) => new Map((items ?? []).map((r) => [r.id, r.name])))
      );
    }
    return this.umRoleService.gets({ pageNumber: 0, pageSize: 500 }).pipe(
      map((page) => new Map((page.items ?? []).map((r) => [r.id, r.name])))
    );
  }

  private resolveRoleLabels(user: GetUserResponse): Observable<string[]> {
    const roleIds = user.roleIds ?? [];
    if (roleIds.length === 0) {
      return of([]);
    }
    return this.loadRoleNameMap().pipe(
      switchMap((byId) => {
        const missing = roleIds.filter((id) => !byId.has(id));
        if (missing.length === 0) {
          return of(roleIds.map((rid) => byId.get(rid) ?? `#${rid}`));
        }
        return forkJoin(
          missing.map((id) =>
            this.umRoleService.get({ id }).pipe(catchError(() => of(null as GetRoleResponse | null)))
          )
        ).pipe(
          map((rows) => {
            for (const row of rows) {
              if (row?.id != null) {
                byId.set(row.id, row.name ?? `Role #${row.id}`);
              }
            }
            return roleIds.map((rid) => byId.get(rid) ?? `#${rid}`);
          })
        );
      })
    );
  }

  displayName(u: GetUserResponse): string {
    return [u.firstName, u.lastName].filter(Boolean).join(' ').trim() || '—';
  }

  edit(): void {
    const id = this.user?.id;
    if (id != null) {
      this.router.navigate(['/um', 'user', id, 'edit']);
    }
  }

  back(): void {
    this.router.navigate(['/um', 'user']);
  }

  delete(): void {
    const id = this.user?.id;
    if (id == null) return;

    const ref = this.dialog.open(SimpleConfirmDialogComponent, {
      data: { title: 'Delete user', message: 'This cannot be undone.', confirmLabel: 'Delete' },
      width: '360px',
    });
    ref.afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.deleting = true;
      this.umUserService
        .delete({ id })
        .pipe(finalize(() => (this.deleting = false)))
        .subscribe({
          next: () => this.router.navigate(['/um', 'user']),
          error: () => {},
        });
    });
  }
}
