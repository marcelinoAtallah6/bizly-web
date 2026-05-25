import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { TravelInvoiceRow, TravelPaymentRow } from 'src/app/core/models/travel.models';
import { TravelKpiItem } from '../shared/components/kpi-strip/travel-kpi-strip.component';
import { TravelInvoiceService } from '../services/travel-invoice.service';
import { TravelPaymentService } from '../services/travel-payment.service';

@Component({
  selector: 'app-travel-finance',
  templateUrl: './travel-finance.component.html',
})
export class TravelFinanceComponent implements OnInit {
  invoices: TravelInvoiceRow[] = [];
  payments: TravelPaymentRow[] = [];
  loading = true;

  get totalInvoiced(): number {
    return this.invoices.reduce((s, i) => s + (Number(i.amount) || 0), 0);
  }

  get totalPaid(): number {
    return this.payments.reduce((s, p) => s + (Number(p.amount) || 0), 0);
  }

  get pending(): number {
    const unpaid = this.invoices.filter((i) => i.status !== 'PAID' && i.status !== 'CANCELLED');
    return unpaid.reduce((s, i) => s + (Number(i.amount) || 0), 0);
  }

  get overdueCount(): number {
    return this.invoices.filter((i) => i.status === 'OVERDUE').length;
  }

  get financeKpis(): TravelKpiItem[] {
    return [
      { label: 'Total invoiced', value: this.totalInvoiced.toFixed(2), icon: 'receipt_long', tone: 'finance' },
      { label: 'Total paid', value: this.totalPaid.toFixed(2), icon: 'payments', tone: 'packages' },
      { label: 'Pending', value: this.pending.toFixed(2), icon: 'schedule', tone: 'open' },
      { label: 'Overdue', value: this.overdueCount, icon: 'warning', tone: 'bookings' },
    ];
  }

  constructor(
    private readonly invoiceApi: TravelInvoiceService,
    private readonly paymentApi: TravelPaymentService
  ) {}

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    this.loading = true;
    forkJoin({
      invoices: this.invoiceApi.gets({ pageNumber: 0, pageSize: 500 }),
      payments: this.paymentApi.gets({ pageNumber: 0, pageSize: 500 }),
    }).subscribe({
      next: (r) => {
        this.invoices = r.invoices.items ?? [];
        this.payments = r.payments.items ?? [];
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }
}
