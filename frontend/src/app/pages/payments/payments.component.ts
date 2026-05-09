import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { ButtonConfig } from '../ui-components/switch/switch.component';
import { Subject, filter, takeUntil } from 'rxjs';

@Component({
  selector: 'app-payments',
  templateUrl: './payments.component.html',
  styleUrl: './payments.component.scss',
})
export class PaymentsComponent implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();

  readonly payTabs: ButtonConfig[] = [
    { value: 'checkout', label: 'Checkout', icon: 'point_of_sale' },
    { value: 'history', label: 'History', icon: 'history' },
  ];

  payTab: 'checkout' | 'history' = 'checkout';

  constructor(private readonly router: Router) {}

  ngOnInit(): void {
    this.syncTabFromUrl(this.router.url);
    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe((e) => this.syncTabFromUrl(e.urlAfterRedirects));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onPayTab(value: string): void {
    if (value === 'history') {
      this.router.navigate(['/pay/history']);
    } else {
      this.router.navigate(['/pay/checkout']);
    }
  }

  private syncTabFromUrl(url: string): void {
    this.payTab = url.includes('/pay/history') || url.endsWith('/history') ? 'history' : 'checkout';
  }
}
