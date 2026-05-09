import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { GetCustomerResponse } from 'src/app/core/models/kyc.models';
import { GetProductResponse } from 'src/app/core/models/pm.models';
import { KycCustomerService } from '../../kyc/services/kyc-customer.service';
import { PmProductService } from '../../pm/services/pm-product.service';
import { PmSaleService } from '../../pm/services/pm-sale.service';

@Component({
  selector: 'app-payment-checkout',
  templateUrl: './payment-checkout.component.html',
  styleUrl: './payment-checkout.component.scss',
})
export class PaymentCheckoutComponent implements OnInit {
  loading = true;
  submitting = false;
  customers: GetCustomerResponse[] = [];
  products: GetProductResponse[] = [];
  cart: Record<number, number> = {};

  checkoutForm: FormGroup<{ customerId: FormControl<number | null> }>;

  constructor(
    private readonly fb: FormBuilder,
    private readonly kyc: KycCustomerService,
    private readonly pmProduct: PmProductService,
    private readonly pmSale: PmSaleService,
    private readonly snack: MatSnackBar
  ) {
    this.checkoutForm = this.fb.group({
      customerId: this.fb.control<number | null>(null),
    });
  }

  ngOnInit(): void {
    this.loading = true;
    forkJoin({
      cust: this.kyc.gets({ pageNumber: 0, pageSize: 500 }),
      prod: this.pmProduct.gets({ pageNumber: 0, pageSize: 500, includeImages: true }),
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: ({ cust, prod }) => {
          this.customers = cust.items ?? [];
          this.products = prod.items ?? [];
        },
        error: () => {},
      });
  }

  get canCompletePayment(): boolean {
    if (this.submitting || this.loading) {
      return false;
    }
    const cid = this.checkoutForm.get('customerId')?.value;
    if (cid === null || cid === undefined) {
      return false;
    }
    return this.cartTotal() > 0;
  }

  customerLabel(c: GetCustomerResponse): string {
    const name = [c.firstName, c.lastName].filter(Boolean).join(' ').trim();
    return name || c.fullName || c.email || `Customer #${c.id}`;
  }

  selectedCustomer(): GetCustomerResponse | undefined {
    const id = this.checkoutForm.get('customerId')?.value;
    if (id == null) return undefined;
    return this.customers.find((c) => c.id === id);
  }

  productImageUrl(p: GetProductResponse): string | null {
    if (p.productImageBase64 && p.productImageMimeType) {
      return `data:${p.productImageMimeType};base64,${p.productImageBase64}`;
    }
    return null;
  }

  /** Shelf stock remaining after items already in cart (null = unlimited). */
  availableToAdd(p: GetProductResponse): number | null {
    const raw = p.stockQuantity;
    if (raw === undefined || raw === null) {
      return null;
    }
    const cap = Number(raw);
    if (!Number.isFinite(cap)) return null;
    return Math.max(0, cap - this.qty(p.id));
  }

  stockBadgeLabel(p: GetProductResponse): string {
    const raw = p.stockQuantity;
    if (raw === undefined || raw === null) return 'Stock: unlimited';
    return `Stock: ${Number(raw)}`;
  }

  canAddMore(p: GetProductResponse): boolean {
    const a = this.availableToAdd(p);
    return a === null || a > 0;
  }

  unitPrice(p: GetProductResponse): number {
    const n = Number(p.price);
    return Number.isFinite(n) ? n : 0;
  }

  qty(productId: number): number {
    return this.cart[productId] ?? 0;
  }

  addProduct(p: GetProductResponse): void {
    if (!this.canAddMore(p)) {
      this.snack.open('No more stock available for this product', 'Dismiss', { duration: 3000 });
      return;
    }
    const q = this.qty(p.id) + 1;
    this.cart = { ...this.cart, [p.id]: q };
  }

  removeProduct(productId: number): void {
    const q = this.qty(productId) - 1;
    const next = { ...this.cart };
    if (q <= 0) {
      delete next[productId];
    } else {
      next[productId] = q;
    }
    this.cart = next;
  }

  lineTotal(p: GetProductResponse): number {
    return this.qty(p.id) * this.unitPrice(p);
  }

  cartTotal(): number {
    return this.products.reduce((sum, p) => sum + this.lineTotal(p), 0);
  }

  cartLines(): { productId: number; quantity: number }[] {
    return Object.entries(this.cart)
      .filter(([, q]) => q > 0)
      .map(([id, quantity]) => ({ productId: Number(id), quantity }));
  }

  cartProducts(): GetProductResponse[] {
    return this.products.filter((p) => this.qty(p.id) > 0);
  }

  trackByProductId(_index: number, p: GetProductResponse): number {
    return p.id;
  }

  checkout(): void {
    const customerId = this.checkoutForm.get('customerId')?.value;
    if (customerId === null || customerId === undefined) {
      this.snack.open('Select a customer', 'Dismiss', { duration: 3500 });
      return;
    }
    const lines = this.cartLines();
    if (!lines.length) {
      this.snack.open('Add at least one product', 'Dismiss', { duration: 3500 });
      return;
    }
    const sel = this.selectedCustomer();
    const customerDisplayName = sel ? this.customerLabel(sel) : undefined;

    this.submitting = true;
    this.pmSale
      .checkout({
        customerId,
        lines,
        ...(customerDisplayName ? { customerDisplayName } : {}),
      })
      .pipe(finalize(() => (this.submitting = false)))
      .subscribe({
        next: (res) => {
          const total =
            typeof res.totalAmount === 'number' ? res.totalAmount.toFixed(2) : String(res.totalAmount);
          this.snack.open(`Sale #${res.saleId} completed. Total ${total}`, 'OK', { duration: 5000 });
          this.cart = {};
          this.reloadProductsAfterSale();
        },
        error: () => {},
      });
  }

  /** Refresh product rows so stock quantities match backend after checkout. */
  private reloadProductsAfterSale(): void {
    this.pmProduct
      .gets({ pageNumber: 0, pageSize: 500, includeImages: true })
      .subscribe({
        next: (page) => {
          this.products = page.items ?? [];
        },
        error: () => {},
      });
  }
}
