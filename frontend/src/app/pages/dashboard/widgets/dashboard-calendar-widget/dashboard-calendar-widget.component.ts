import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
  TemplateRef,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { TemplatePortal } from '@angular/cdk/portal';
import { Router } from '@angular/router';

export interface DashboardCalendarEvent {
  date: string;
  title: string;
  type: string;
  route?: string;
  meta?: Record<string, unknown>;
}

export interface DashboardCalendarConfig {
  dateField?: string;
  titleField?: string;
  typeField?: string;
  routeField?: string;
  idField?: string;
  bookingRouteBase?: string;
  colorMap?: Record<string, string>;
  travelOnlyFilter?: boolean;
  travelEventTypes?: string[];
}

interface CalendarDayCell {
  date: Date;
  inMonth: boolean;
  isToday: boolean;
  key: string;
}

@Component({
  selector: 'app-dashboard-calendar-widget',
  templateUrl: './dashboard-calendar-widget.component.html',
  styleUrl: './dashboard-calendar-widget.component.scss',
})
export class DashboardCalendarWidgetComponent implements OnChanges, OnDestroy {
  @Input() rows: Record<string, unknown>[] = [];
  @Input() config: DashboardCalendarConfig | null = null;
  @Input() loading = false;

  @Output() eventClick = new EventEmitter<DashboardCalendarEvent>();

  viewMonth = new Date();
  weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  cells: CalendarDayCell[] = [];
  events: DashboardCalendarEvent[] = [];
  filteredEvents: DashboardCalendarEvent[] = [];
  eventTypes: string[] = [];
  selectedTypes = new Set<string>();
  travelOnly = false;

  panelOpen = false;
  selectedDay: CalendarDayCell | null = null;
  dayEvents: DashboardCalendarEvent[] = [];

  @ViewChild('popoverTpl') popoverTpl!: TemplateRef<unknown>;

  popoverEvent: DashboardCalendarEvent | null = null;
  private popoverTimer: ReturnType<typeof setTimeout> | null = null;
  private overlayRef: OverlayRef | null = null;
  private popoverAnchorEl: HTMLElement | null = null;

  private readonly maxChipsPerDay = 2;
  private readonly popoverWidth = 300;
  private readonly popoverHeight = 210;

  constructor(
    private readonly router: Router,
    private readonly overlay: Overlay,
    private readonly viewContainerRef: ViewContainerRef
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['rows'] || changes['config']) {
      this.parseEvents();
      this.applyFilters();
      this.buildGrid();
    }
  }

  prevMonth(): void {
    this.viewMonth = new Date(this.viewMonth.getFullYear(), this.viewMonth.getMonth() - 1, 1);
    this.buildGrid();
  }

  nextMonth(): void {
    this.viewMonth = new Date(this.viewMonth.getFullYear(), this.viewMonth.getMonth() + 1, 1);
    this.buildGrid();
  }

  goToday(): void {
    this.viewMonth = new Date();
    this.buildGrid();
  }

  get monthLabel(): string {
    return this.viewMonth.toLocaleDateString(undefined, { month: 'long', year: 'numeric' });
  }

  toggleType(type: string): void {
    if (this.selectedTypes.has(type)) {
      this.selectedTypes.delete(type);
    } else {
      this.selectedTypes.add(type);
    }
    this.applyFilters();
    this.buildGrid();
    if (this.selectedDay) {
      this.dayEvents = this.eventsOnDay(this.selectedDay);
    }
  }

  toggleTravelOnly(): void {
    this.travelOnly = !this.travelOnly;
    this.applyFilters();
    this.buildGrid();
    if (this.selectedDay) {
      this.dayEvents = this.eventsOnDay(this.selectedDay);
    }
  }

  openDay(cell: CalendarDayCell): void {
    this.selectedDay = cell;
    this.dayEvents = this.eventsOnDay(cell);
    this.panelOpen = true;
  }

  closePanel(): void {
    this.panelOpen = false;
    this.selectedDay = null;
    this.dayEvents = [];
  }

  colorFor(type: string): string {
    const map = this.config?.colorMap ?? {};
    return map[type] ?? map['DEFAULT'] ?? '#5D87FF';
  }

  eventTypeLabel(type: string): string {
    return (type || '').replace(/_/g, ' ');
  }

  onEventClick(ev: DashboardCalendarEvent, $event?: Event): void {
    $event?.stopPropagation();
    $event?.preventDefault();
    this.detachPopover();
    this.eventClick.emit(ev);
    const target = this.navigationUrl(ev);
    if (target) {
      void this.router.navigateByUrl(target);
    }
  }

  eventsOnDay(cell: CalendarDayCell): DashboardCalendarEvent[] {
    return this.filteredEvents.filter((e) => e.date === cell.key);
  }

  visibleEventsForDay(cell: CalendarDayCell): DashboardCalendarEvent[] {
    return this.eventsOnDay(cell).slice(0, this.maxChipsPerDay);
  }

  extraCountForDay(cell: CalendarDayCell): number {
    const total = this.eventsOnDay(cell).length;
    return Math.max(0, total - this.maxChipsPerDay);
  }

  shortTitle(title: string): string {
    const t = (title || '').trim();
    return t.length > 22 ? `${t.slice(0, 20)}…` : t;
  }

  shortType(type: string): string {
    const t = (type || '').replace(/_/g, ' ');
    return t.length > 8 ? t.slice(0, 7) + '…' : t;
  }

  showPopover(ev: DashboardCalendarEvent, anchor: MouseEvent): void {
    this.cancelHidePopover();
    this.popoverEvent = ev;
    const el = anchor.currentTarget as HTMLElement | null;
    if (!el || !this.popoverTpl) {
      return;
    }
    this.popoverAnchorEl = el;
    this.detachPopover(false);
    this.openPopoverOverlay();
  }

  private openPopoverOverlay(): void {
    const el = this.popoverAnchorEl;
    if (!el || !this.popoverTpl || !this.popoverEvent) {
      return;
    }
    const { left, top } = this.computePopoverPosition(el);
    const strategy = this.overlay.position().global().left(`${left}px`).top(`${top}px`);
    this.overlayRef = this.overlay.create({
      positionStrategy: strategy,
      scrollStrategy: this.overlay.scrollStrategies.close(),
      panelClass: 'dash-cal-popover-overlay',
      hasBackdrop: false,
    });
    this.overlayRef.attach(new TemplatePortal(this.popoverTpl, this.viewContainerRef));
  }

  /** Place the card beside the hovered chip, flipping when near viewport edges. */
  private computePopoverPosition(anchor: HTMLElement): { left: number; top: number } {
    const rect = anchor.getBoundingClientRect();
    const gap = 10;
    const margin = 12;
    const vw = window.innerWidth;
    const vh = window.innerHeight;
    const w = this.popoverWidth;
    const h = this.popoverHeight;

    let left = rect.right + gap;
    let top = rect.top + rect.height / 2 - h / 2;

    if (left + w > vw - margin) {
      left = rect.left - w - gap;
    }
    if (left < margin) {
      left = Math.min(rect.left, vw - w - margin);
      top = rect.bottom + gap;
    }
    if (top + h > vh - margin) {
      top = vh - h - margin;
    }
    if (top < margin) {
      top = margin;
    }
    return { left, top };
  }

  hidePopoverSoon(): void {
    if (this.popoverTimer) {
      clearTimeout(this.popoverTimer);
    }
    this.popoverTimer = setTimeout(() => {
      this.detachPopover();
    }, 200);
  }

  cancelHidePopover(): void {
    if (this.popoverTimer) {
      clearTimeout(this.popoverTimer);
      this.popoverTimer = null;
    }
  }

  ngOnDestroy(): void {
    this.detachPopover();
  }

  private detachPopover(clearEvent = true): void {
    if (this.overlayRef) {
      this.overlayRef.detach();
      this.overlayRef.dispose();
      this.overlayRef = null;
    }
    if (clearEvent) {
      this.popoverAnchorEl = null;
      this.popoverEvent = null;
    }
  }

  popoverMetaLine(ev: DashboardCalendarEvent): string | null {
    const id = this.metaValue(ev, (this.config?.idField ?? 'BOOKING_ID').toUpperCase());
    if (id != null && String(id).trim() !== '') {
      return `Booking #${id}`;
    }
    return null;
  }

  navigationUrl(ev: DashboardCalendarEvent): string | null {
    const cfg = this.config ?? {};
    const idField = (cfg.idField ?? 'BOOKING_ID').toUpperCase();
    const bookingBase = (cfg.bookingRouteBase ?? '/travel/bookings').replace(/\/$/, '');
    const bookingId = this.metaValue(ev, idField);
    const type = (ev.type || '').toUpperCase();

    if (bookingId != null && String(bookingId).trim() !== '') {
      const id = encodeURIComponent(String(bookingId));
      if (type === 'DEPARTURE' || type === 'RETURN') {
        return `${bookingBase}?id=${id}`;
      }
      if (type === 'FOLLOW_UP') {
        return `/travel/follow-ups?bookingId=${id}`;
      }
      if (type === 'PAYMENT') {
        return `/travel/finance?bookingId=${id}`;
      }
    }

    if (type === 'FOLLOW_UP') {
      return '/travel/follow-ups';
    }
    if (type === 'PAYMENT') {
      return '/travel/finance';
    }
    if (type === 'VISA') {
      return '/travel/visas';
    }

    const route = ev.route?.trim();
    return route || null;
  }

  private metaValue(ev: DashboardCalendarEvent, field: string): unknown {
    const meta = ev.meta ?? {};
    if (meta[field] !== undefined) {
      return meta[field];
    }
    const lower = field.toLowerCase();
    return meta[lower];
  }

  private parseEvents(): void {
    const cfg = this.config ?? {};
    const dateField = (cfg.dateField ?? 'EVENT_DATE').toUpperCase();
    const titleField = (cfg.titleField ?? 'TITLE').toUpperCase();
    const typeField = (cfg.typeField ?? 'EVENT_TYPE').toUpperCase();
    const routeField = (cfg.routeField ?? 'ROUTE').toUpperCase();
    const idField = (cfg.idField ?? 'BOOKING_ID').toUpperCase();

    const keyOf = (row: Record<string, unknown>, field: string): unknown => {
      const direct = row[field];
      if (direct !== undefined) {
        return direct;
      }
      const lower = field.toLowerCase();
      return row[lower] ?? row[field];
    };

    const parsed: DashboardCalendarEvent[] = [];
    for (const row of this.rows ?? []) {
      const rawDate = keyOf(row, dateField);
      const iso = this.normalizeDate(rawDate);
      if (!iso) {
        continue;
      }
      const type = String(keyOf(row, typeField) ?? 'EVENT').toUpperCase();
      const title = String(keyOf(row, titleField) ?? type);
      const route = keyOf(row, routeField);
      const bookingId = keyOf(row, idField);
      parsed.push({
        date: iso,
        title,
        type,
        route: route != null ? String(route) : undefined,
        meta: { ...row, BOOKING_ID: bookingId ?? row['BOOKING_ID'] ?? row['booking_id'] },
      });
    }
    this.events = parsed;

    this.travelOnly = !!cfg.travelOnlyFilter;
    const types = new Set(this.events.map((e) => e.type));
    this.eventTypes = [...types].sort();
    if (!this.selectedTypes.size) {
      this.selectedTypes = new Set(this.eventTypes);
    }
  }

  private applyFilters(): void {
    const travelTypes = new Set(
      (this.config?.travelEventTypes ?? ['DEPARTURE', 'RETURN', 'FOLLOW_UP', 'PAYMENT', 'VISA']).map((t) =>
        t.toUpperCase()
      )
    );
    this.filteredEvents = this.events.filter((e) => {
      if (this.travelOnly && !travelTypes.has(e.type)) {
        return false;
      }
      if (this.selectedTypes.size && !this.selectedTypes.has(e.type)) {
        return false;
      }
      return true;
    });
  }

  private buildGrid(): void {
    const y = this.viewMonth.getFullYear();
    const m = this.viewMonth.getMonth();
    const first = new Date(y, m, 1);
    const start = new Date(y, m, 1 - first.getDay());
    const todayKey = this.dateKey(new Date());
    const cells: CalendarDayCell[] = [];
    for (let i = 0; i < 42; i++) {
      const d = new Date(start.getFullYear(), start.getMonth(), start.getDate() + i);
      cells.push({
        date: d,
        inMonth: d.getMonth() === m,
        isToday: this.dateKey(d) === todayKey,
        key: this.dateKey(d),
      });
    }
    this.cells = cells;
  }

  private normalizeDate(raw: unknown): string | null {
    if (raw == null) {
      return null;
    }
    if (raw instanceof Date) {
      return this.dateKey(raw);
    }
    const s = String(raw).trim();
    if (!s) {
      return null;
    }
    if (/^\d{4}-\d{2}-\d{2}/.test(s)) {
      return s.slice(0, 10);
    }
    const d = new Date(s);
    if (Number.isNaN(d.getTime())) {
      return null;
    }
    return this.dateKey(d);
  }

  private dateKey(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }
}
