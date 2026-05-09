import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { finalize, map } from 'rxjs/operators';
import { GetUserResponse } from 'src/app/core/models/um.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { SimpleConfirmDialogComponent } from 'src/app/shared/dialogs/simple-confirm-dialog.component';
import { UmRoleService } from '../../services/um-role.service';
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
    return [
      { id: 'back', icon: 'arrow_back', tooltip: 'Back to list', action: () => this.back() },
      {
        id: 'delete',
        icon: 'delete_outline',
        tooltip: 'Delete user',
        action: () => this.delete(),
        disabled: !this.user || this.deleting,
        color: 'warn',
      },
      {
        id: 'edit',
        icon: 'edit',
        tooltip: 'Edit user',
        action: () => this.edit(),
        disabled: !this.user,
        color: 'primary',
      },
    ];
  }

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly umUserService: UmUserService,
    private readonly umRoleService: UmRoleService,
    private readonly dialog: MatDialog
  ) {}

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
    forkJoin({
      user: this.umUserService.get({ id }),
      rolesPage: this.umRoleService.gets({ pageNumber: 0, pageSize: 500 }),
    })
      .pipe(
        map(({ user, rolesPage }) => {
          const byId = new Map((rolesPage.items ?? []).map((r) => [r.id, r.name]));
          const labels = (user.roleIds ?? []).map((rid) => byId.get(rid) ?? `#${rid}`);
          return { user, roleLabels: labels };
        }),
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
