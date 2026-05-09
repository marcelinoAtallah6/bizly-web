import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { PmProductService } from '../../services/pm-product.service';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.scss',
})
export class ProductFormComponent implements OnInit {
  form!: FormGroup;
  mode: 'create' | 'edit' = 'create';
  productId: number | null = null;
  loading = false;
  saving = false;

  productPreviewUrl: string | null = null;
  pendingProductImage: { mime: string; base64: string } | null = null;
  removeProductPhoto = false;

  get formToolbar(): ToolbarButton[] {
    return [{ id: 'back', icon: 'arrow_back', tooltip: 'Back to list', action: () => this.cancel() }];
  }

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly pmProductService: PmProductService
  ) {}

  ngOnInit(): void {
    this.mode = (this.route.snapshot.data['mode'] as 'create' | 'edit') ?? 'create';
    const idParam = this.route.snapshot.paramMap.get('id');
    this.productId = idParam ? Number(idParam) : null;

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      price: [null as number | null, [Validators.required, Validators.min(0)]],
      stockQuantity: [null as number | null, [Validators.min(0)]],
    });

    if (this.mode === 'edit' && this.productId != null && !Number.isNaN(this.productId)) {
      this.loadProduct(this.productId);
    }
  }

  cancel(): void {
    this.router.navigate(['/pm/products']);
  }

  onProductFileSelected(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file || !file.type.startsWith('image/')) {
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const dataUrl = reader.result as string;
      const comma = dataUrl.indexOf(',');
      const base64 = comma >= 0 ? dataUrl.slice(comma + 1) : dataUrl;
      this.pendingProductImage = { mime: file.type, base64 };
      this.productPreviewUrl = dataUrl;
      this.removeProductPhoto = false;
    };
    reader.readAsDataURL(file);
    input.value = '';
  }

  clearProductPhoto(): void {
    this.pendingProductImage = null;
    this.productPreviewUrl = null;
    this.removeProductPhoto = true;
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.getRawValue();
    const name = String(v.name).trim();
    const price = Number(v.price);
    const stockRaw = v.stockQuantity;
    const stockQuantity =
      stockRaw === '' || stockRaw === null || stockRaw === undefined
        ? undefined
        : Number(stockRaw);

    this.saving = true;
    if (this.mode === 'create') {
      this.pmProductService
        .add({
          name,
          price,
          ...(stockQuantity !== undefined && Number.isFinite(stockQuantity) ? { stockQuantity } : {}),
          ...(this.pendingProductImage
            ? {
                productImageMimeType: this.pendingProductImage.mime,
                productImageBase64: this.pendingProductImage.base64,
              }
            : {}),
        })
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: () => this.router.navigate(['/pm/products']),
          error: () => {},
        });
    } else if (this.productId != null) {
      this.pmProductService
        .update({
          id: this.productId,
          name,
          price,
          ...(stockQuantity !== undefined && Number.isFinite(stockQuantity) ? { stockQuantity } : {}),
          ...(this.removeProductPhoto ? { clearProductImage: true } : {}),
          ...(!this.removeProductPhoto && this.pendingProductImage
            ? {
                productImageMimeType: this.pendingProductImage.mime,
                productImageBase64: this.pendingProductImage.base64,
              }
            : {}),
        })
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: () => this.router.navigate(['/pm/products', this.productId]),
          error: () => {},
        });
    }
  }

  private loadProduct(id: number): void {
    this.loading = true;
    this.pmProductService
      .get({ id })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (row) => {
          this.form.patchValue({
            name: row.name ?? '',
            price: row.price ?? null,
            stockQuantity: row.stockQuantity ?? null,
          });
          if (row.productImageBase64 && row.productImageMimeType) {
            this.productPreviewUrl = `data:${row.productImageMimeType};base64,${row.productImageBase64}`;
          }
        },
        error: () => {},
      });
  }
}
