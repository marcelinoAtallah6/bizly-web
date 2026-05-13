import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, Subscription } from 'rxjs';
import { distinctUntilChanged, filter, map } from 'rxjs/operators';
import { GetRoleResponse } from 'src/app/core/models/um.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { SimpleConfirmDialogComponent } from 'src/app/shared/dialogs/simple-confirm-dialog.component';
import { UmRoleService } from '../../services/um-role.service';

@Component({
  selector: 'app-role-details',
  templateUrl: './role-details.component.html',
  styleUrl: './role-details.component.scss',
})
export class RoleDetailsComponent implements OnInit, OnDestroy {
  role: GetRoleResponse | null = null;
  loading = false;
  deleting = false;

  private routeSub?: Subscription;

  get detailToolbar(): ToolbarButton[] {
    const buttons: ToolbarButton[] = [
      { id: 'back', icon: 'arrow_back', tooltip: 'Back to list', action: () => this.back() },
    ];
    if (this.menuPerm.can('/um/role', 'delete')) {
      buttons.push({
        id: 'delete',
        icon: 'delete_outline',
        tooltip: 'Delete role',
        action: () => this.delete(),
        disabled: !this.role || this.deleting,
        color: 'warn',
      });
    }
    if (this.menuPerm.can('/um/role', 'edit')) {
      buttons.push({
        id: 'edit',
        icon: 'edit',
        tooltip: 'Edit role',
        action: () => this.edit(),
        disabled: !this.role,
        color: 'primary',
      });
    }
    return buttons;
  }

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly umRoleService: UmRoleService,
    private readonly dialog: MatDialog,
    private readonly menuPerm: MenuPermissionService
  ) {}

  ngOnInit(): void {
    this.routeSub = this.route.paramMap
      .pipe(
        map((p) => Number(p.get('id'))),
        filter((id) => Number.isFinite(id)),
        distinctUntilChanged()
      )
      .subscribe((id) => this.load(id));
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
  }

  load(id: number): void {
    this.loading = true;
    this.umRoleService
      .get({ id })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (row) => {
          this.role = row;
        },
        error: () => {},
      });
  }

  edit(): void {
    const id = this.role?.id;
    if (id != null) {
      this.router.navigate(['/um', 'role', id, 'edit']);
    }
  }

  back(): void {
    this.router.navigate(['/um', 'role']);
  }

  delete(): void {
    const id = this.role?.id;
    if (id == null) return;

    const ref = this.dialog.open(SimpleConfirmDialogComponent, {
      data: { title: 'Delete role', message: 'This cannot be undone.', confirmLabel: 'Delete' },
      width: '360px',
    });
    ref.afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.deleting = true;
      this.umRoleService
        .delete({ id })
        .pipe(finalize(() => (this.deleting = false)))
        .subscribe({
          next: () => this.router.navigate(['/um', 'role']),
          error: () => {},
        });
    });
  }
}
