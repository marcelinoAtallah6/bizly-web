import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, forkJoin, of } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { PageResponse } from 'src/app/core/models/api.types';
import { GetServiceItemResponse } from 'src/app/core/models/bm.models';
import { GetCustomerResponse } from 'src/app/core/models/kyc.models';
import { CheckoutLineRequest, GetProductResponse } from 'src/app/core/models/pm.models';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { BmServiceItemService } from '../../bm/services/bm-service-item.service';
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
  services: GetServiceItemResponse[] = [];
  /** productId -> qty */
  cart: Record<number, number> = {};
  /** serviceId -> qty */
  serviceCart: Record<number, number> = {};

  checkoutForm: FormGroup<{ customerId: FormControl<number | null> }>;

  /** Cached at init so the template can hide the entire tab when the role has no view permission. */
  canViewProducts = true;
  canViewServices = true;

  readonly routeProducts = '/pm/products';
  readonly routeBmServices = '/bm/services';

  constructor(
    private readonly fb: FormBuilder,
    private readonly kyc: KycCustomerService,
    private readonly pmProduct: PmProductService,
    private readonly bmServices: BmServiceItemService,
    private readonly pmSale: PmSaleService,
    private readonly snack: MatSnackBar,
    private readonly menuPerm: MenuPermissionService
  ) {
    this.checkoutForm = this.fb.group({
      customerId: this.fb.control<number | null>(null),
    });
  }

  ngOnInit(): void {
    this.canViewProducts = this.menuPerm.can(this.routeProducts, 'view');
    this.canViewServices = this.menuPerm.can(this.routeBmServices, 'view');

    this.loading = true;
    const emptyPage = <T,>(): PageResponse<T> => ({
      items: [] as T[],
      totalCount: 0,
      pageNumber: 0,
      pageSize: 0,
      totalPages: 0,
    });
    forkJoin({
      cust: this.kyc.gets({ pageNumber: 0, pageSize: 500 }),
      prod: this.canViewProducts
        ? this.pmProduct.gets({ pageNumber: 0, pageSize: 500, includeImages: true })
        : (of(emptyPage<GetProductResponse>()) as Observable<PageResponse<GetProductResponse>>),
      svc: this.canViewServices
        ? this.bmServices.gets({ pageNumber: 0, pageSize: 1000, includeInactive: false })
        : (of(emptyPage<GetServiceItemResponse>()) as Observable<PageResponse<GetServiceItemResponse>>),
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: ({ cust, prod, svc }) => {
          this.customers = cust.items ?? [];
          this.products = prod.items ?? [];
          this.services = (svc.items ?? []).filter((s) => s.active !== false);
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

  /** --- Services (BM) — no stock cap */
  unitPriceService(s: GetServiceItemResponse): number {
    const n = Number(s.price);
    return Number.isFinite(n) ? n : 0;
  }

  qtyService(serviceId: number): number {
    return this.serviceCart[serviceId] ?? 0;
  }

  addService(s: GetServiceItemResponse): void {
    const q = this.qtyService(s.id) + 1;
    this.serviceCart = { ...this.serviceCart, [s.id]: q };
  }

  removeService(serviceId: number): void {
    const q = this.qtyService(serviceId) - 1;
    const next = { ...this.serviceCart };
    if (q <= 0) {
      delete next[serviceId];
    } else {
      next[serviceId] = q;
    }
    this.serviceCart = next;
  }

  lineTotalService(s: GetServiceItemResponse): number {
    return this.qtyService(s.id) * this.unitPriceService(s);
  }

  cartTotal(): number {
    const productSum = this.products.reduce((sum, p) => sum + this.lineTotal(p), 0);
    const serviceSum = this.services.reduce((sum, s) => sum + this.lineTotalService(s), 0);
    return productSum + serviceSum;
  }

  checkoutLines(): CheckoutLineRequest[] {
    const productLines: CheckoutLineRequest[] = Object.entries(this.cart)
      .filter(([, q]) => q > 0)
      .map(([id, quantity]) => ({ productId: Number(id), quantity }))
      .filter((l) => Number.isFinite(l.productId) && l.productId > 0);
    const serviceLines: CheckoutLineRequest[] = Object.entries(this.serviceCart)
      .filter(([, q]) => q > 0)
      .map(([id, quantity]) => ({ serviceId: Number(id), quantity }))
      .filter((l) => Number.isFinite(l.serviceId) && l.serviceId > 0);
    return [...productLines, ...serviceLines];
  }

  cartProducts(): GetProductResponse[] {
    return this.products.filter((p) => this.qty(p.id) > 0);
  }

  cartServices(): GetServiceItemResponse[] {
    return this.services.filter((s) => this.qtyService(s.id) > 0);
  }

  trackByProductId(_index: number, p: GetProductResponse): number {
    return p.id;
  }

  trackByServiceId(_index: number, s: GetServiceItemResponse): number {
    return s.id;
  }

  checkout(): void {
    const customerId = this.checkoutForm.get('customerId')?.value;
    if (customerId === null || customerId === undefined) {
      this.snack.open('Select a customer', 'Dismiss', { duration: 3500 });
      return;
    }
    const lines = this.checkoutLines();
    if (!lines.length) {
      this.snack.open('Add at least one product or service', 'Dismiss', { duration: 3500 });
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
          this.serviceCart = {};
          this.reloadCatalogAfterSale();
        },
        error: () => {},
      });
  }

  private reloadCatalogAfterSale(): void {
    if (this.canViewProducts) {
      this.pmProduct.gets({ pageNumber: 0, pageSize: 500, includeImages: true }).subscribe({
        next: (page) => {
          this.products = page.items ?? [];
        },
        error: () => {},
      });
    }
    if (this.canViewServices) {
      this.bmServices.gets({ pageNumber: 0, pageSize: 1000, includeInactive: false }).subscribe({
        next: (page) => {
          this.services = (page.items ?? []).filter((s) => s.active !== false);
        },
        error: () => {},
      });
    }
  }
}
