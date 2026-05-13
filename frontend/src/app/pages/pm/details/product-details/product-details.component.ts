import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { GetProductResponse } from 'src/app/core/models/pm.models';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { SimpleConfirmDialogComponent } from 'src/app/shared/dialogs/simple-confirm-dialog.component';
import { PmProductService } from '../../services/pm-product.service';

@Component({
  selector: 'app-product-details',
  templateUrl: './product-details.component.html',
  styleUrl: './product-details.component.scss',
})
export class ProductDetailsComponent implements OnInit {
  product: GetProductResponse | null = null;
  productImageUrl: string | null = null;
  loading = false;
  deleting = false;

  private readonly routeProducts = '/pm/products';

  get detailToolbar(): ToolbarButton[] {
    return [
      { id: 'back', icon: 'arrow_back', tooltip: 'Back to list', action: () => this.back() },
      {
        id: 'delete',
        icon: 'delete_outline',
        tooltip: 'Delete product',
        action: () => this.delete(),
        disabled: !this.product || this.deleting,
        color: 'warn',
        hidden: !this.menuPerm.can(this.routeProducts, 'delete'),
      },
      {
        id: 'edit',
        icon: 'edit',
        tooltip: 'Edit product',
        action: () => this.edit(),
        disabled: !this.product,
        color: 'primary',
        hidden: !this.menuPerm.can(this.routeProducts, 'edit'),
      },
    ];
  }

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly pmProductService: PmProductService,
    private readonly dialog: MatDialog,
    private readonly menuPerm: MenuPermissionService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id)) {
      this.router.navigate(['/pm/products']);
      return;
    }
    this.load(id);
  }

  load(id: number): void {
    this.loading = true;
    this.pmProductService
      .get({ id })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (row) => {
          this.product = row;
          if (row.productImageBase64 && row.productImageMimeType) {
            this.productImageUrl = `data:${row.productImageMimeType};base64,${row.productImageBase64}`;
          } else {
            this.productImageUrl = null;
          }
        },
        error: () => {},
      });
  }

  edit(): void {
    const id = this.product?.id;
    if (id != null) {
      this.router.navigate(['/pm/products', id, 'edit']);
    }
  }

  back(): void {
    this.router.navigate(['/pm/products']);
  }

  delete(): void {
    const id = this.product?.id;
    if (id == null) return;

    const ref = this.dialog.open(SimpleConfirmDialogComponent, {
      data: { title: 'Delete product', message: 'This cannot be undone.', confirmLabel: 'Delete' },
      width: '360px',
    });
    ref.afterClosed().subscribe((ok) => {
      if (!ok) return;
      this.deleting = true;
      this.pmProductService
        .delete({ id })
        .pipe(finalize(() => (this.deleting = false)))
        .subscribe({
          next: () => this.router.navigate(['/pm/products']),
          error: () => {},
        });
    });
  }
}
