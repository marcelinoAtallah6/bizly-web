import { Component, Input } from '@angular/core';

export interface TravelKpiItem {
  label: string;
  value: string | number;
  icon?: string;
  tone?: 'clients' | 'packages' | 'bookings' | 'open' | 'finance' | 'default';
  hint?: string;
}

@Component({
  selector: 'app-travel-kpi-strip',
  templateUrl: './travel-kpi-strip.component.html',
  styleUrl: './travel-kpi-strip.component.scss',
})
export class TravelKpiStripComponent {
  @Input() items: TravelKpiItem[] = [];
  /** Horizontal scroll for KPI cards (default on all travel screens). */
  @Input() horizontal = true;
}
