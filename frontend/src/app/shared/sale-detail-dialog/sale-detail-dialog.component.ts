import { Component, HostBinding, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { GetSaleResponse } from 'src/app/core/models/pm.models';

@Component({
  selector: 'app-sale-detail-dialog',
  templateUrl: './sale-detail-dialog.component.html',
  styleUrl: './sale-detail-dialog.component.scss',
})
export class SaleDetailDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) readonly data: GetSaleResponse) {}

  /** Same wrapper classes as ui-components dialog so theme tokens apply in light/dark mode */
  @HostBinding('class') hostClass = 'dialog-container dialog sale-detail-host';

  get customerIdText(): string {
    const d = this.data as unknown as Record<string, unknown>;
    const raw = d['customerId'] ?? d['customer_id'] ?? this.data.customerId;
    if (raw === undefined || raw === null || raw === '') {
      return '—';
    }
    const id = Number(raw);
    return Number.isFinite(id) ? String(id) : '—';
  }

  get totalText(): string {
    const d = this.data as unknown as Record<string, unknown>;
    const raw = d['totalAmount'] ?? d['total_amount'] ?? this.data.totalAmount;
    if (raw === undefined || raw === null || raw === '') {
      return '—';
    }
    const t = Number(raw);
    return Number.isFinite(t) ? t.toFixed(2) : '—';
  }
}
