import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';

export interface DashboardCardListConfig {
  titleField?: string;
  subtitleField?: string;
  metaField?: string;
  channelField?: string;
  actionLabel?: string;
  actionRoute?: string;
  routeIdField?: string;
}

@Component({
  selector: 'app-dashboard-card-list-widget',
  templateUrl: './dashboard-card-list-widget.component.html',
  styleUrl: './dashboard-card-list-widget.component.scss',
})
export class DashboardCardListWidgetComponent implements OnChanges {
  @Input() rows: Record<string, unknown>[] = [];
  @Input() config: DashboardCardListConfig | null = null;

  @Output() actionClick = new EventEmitter<Record<string, unknown>>();

  cards: {
    title: string;
    subtitle: string;
    channel: string;
    meta: string;
    row: Record<string, unknown>;
  }[] = [];

  constructor(private readonly router: Router) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['rows'] || changes['config']) {
      this.buildCards();
    }
  }

  onAction(row: Record<string, unknown>): void {
    this.actionClick.emit(row);
    const cfg = this.config ?? {};
    const base = (cfg.actionRoute ?? '').trim();
    if (!base) {
      return;
    }
    const idField = (cfg.routeIdField ?? 'BOOKING_ID').toUpperCase();
    const id = this.pick(row, idField);
    void this.router.navigateByUrl(base);
  }

  private buildCards(): void {
    const cfg = this.config ?? {};
    const titleField = (cfg.titleField ?? 'CLIENT_NAME').toUpperCase();
    const subtitleField = (cfg.subtitleField ?? 'FOLLOW_UP_TYPE').toUpperCase();
    const channelField = (cfg.channelField ?? 'CHANNEL').toUpperCase();
    const metaField = (cfg.metaField ?? 'NOTES').toUpperCase();

    this.cards = (this.rows ?? []).map((row) => ({
      title: String(this.pick(row, titleField) ?? '—'),
      subtitle: String(this.pick(row, subtitleField) ?? '—'),
      channel: String(this.pick(row, channelField) ?? ''),
      meta: String(this.pick(row, metaField) ?? ''),
      row,
    }));
  }

  private pick(row: Record<string, unknown>, field: string): unknown {
    if (row[field] !== undefined) {
      return row[field];
    }
    const lower = field.toLowerCase();
    return row[lower];
  }
}
