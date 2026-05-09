import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { GetRoleResponse } from 'src/app/core/models/um.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { SimpleConfirmDialogComponent } from 'src/app/shared/dialogs/simple-confirm-dialog.component';
import { UmRoleService } from '../../services/um-role.service';

@Component({
  selector: 'app-role-details',
  templateUrl: './role-details.component.html',
  styleUrl: './role-details.component.scss',
})
export class RoleDetailsComponent implements OnInit {
  role: GetRoleResponse | null = null;
  loading = false;
  deleting = false;

  get detailToolbar(): ToolbarButton[] {
    return [
      { id: 'back', icon: 'arrow_back', tooltip: 'Back to list', action: () => this.back() },
      {
        id: 'delete',
        icon: 'delete_outline',
        tooltip: 'Delete role',
        action: () => this.delete(),
        disabled: !this.role || this.deleting,
        color: 'warn',
      },
      {
        id: 'edit',
        icon: 'edit',
        tooltip: 'Edit role',
        action: () => this.edit(),
        disabled: !this.role,
        color: 'primary',
      },
    ];
  }

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly umRoleService: UmRoleService,
    private readonly dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id)) {
      this.router.navigate(['/um', 'role']);
      return;
    }
    this.load(id);
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
