import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { MatCalendarCellClassFunction } from '@angular/material/datepicker';
import { TravelAvailabilityStatus } from 'src/app/core/models/travel.models';
import { TravelBookingService } from '../../../services/travel-booking.service';

@Component({
  selector: 'app-travel-booking-availability-calendar',
  templateUrl: './travel-booking-availability-calendar.component.html',
  styleUrl: './travel-booking-availability-calendar.component.scss',
})
export class TravelBookingAvailabilityCalendarComponent implements OnInit, OnChanges {
  @Input() packageId: number | null = null;
  @Input() selected: Date | null = null;
  @Input() excludeBookingId?: number;
  @Output() selectedChange = new EventEmitter<Date | null>();

  calendarMonth = new Date();
  private statusByIso = new Map<string, TravelAvailabilityStatus>();
  private lastLoadedKey = '';
  loading = false;

  readonly dateClass: MatCalendarCellClassFunction<Date> = (cellDate, view) => {
    if (view !== 'month') {
      return '';
    }
    const key = this.toIso(cellDate);
    const st = this.statusByIso.get(key);
    if (!st) {
      return '';
    }
    return `travel-cal-${st.toLowerCase()}`;
  };

  constructor(private readonly bookingApi: TravelBookingService) {}

  ngOnInit(): void {
    this.loadMonth(this.calendarMonth);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['packageId'] || changes['excludeBookingId']) {
      this.loadMonth(this.calendarMonth);
    }
  }

  prevMonth(): void {
    const d = new Date(this.calendarMonth.getFullYear(), this.calendarMonth.getMonth() - 1, 1);
    this.calendarMonth = d;
    this.loadMonth(d);
  }

  nextMonth(): void {
    const d = new Date(this.calendarMonth.getFullYear(), this.calendarMonth.getMonth() + 1, 1);
    this.calendarMonth = d;
    this.loadMonth(d);
  }

  onDateSelected(date: Date | null): void {
    if (!date) {
      return;
    }
    const key = this.toIso(date);
    const st = this.statusByIso.get(key);
    if (st === 'BOOKED' || st === 'UNAVAILABLE') {
      return;
    }
    this.selected = date;
    this.selectedChange.emit(date);
  }

  private loadMonth(ref: Date): void {
    const year = ref.getFullYear();
    const month = ref.getMonth() + 1;
    const key = `${year}-${month}`;
    if (key === this.lastLoadedKey && this.statusByIso.size > 0) {
      return;
    }
    this.lastLoadedKey = key;
    this.loading = true;
    this.bookingApi
      .availability({
        packageId: this.packageId ?? undefined,
        year,
        month,
        excludeBookingId: this.excludeBookingId,
      })
      .subscribe({
        next: (res) => {
          this.statusByIso.clear();
          for (const d of res.days ?? []) {
            if (d.date) {
              this.statusByIso.set(d.date.substring(0, 10), d.status);
            }
          }
          this.loading = false;
        },
        error: () => (this.loading = false),
      });
  }

  private toIso(d: Date): string {
    const y = d.getFullYear();
    const mo = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${mo}-${day}`;
  }

  legend(): { label: string; className: string }[] {
    return [
      { label: 'Available', className: 'travel-cal-available' },
      { label: 'Pending', className: 'travel-cal-pending' },
      { label: 'Booked', className: 'travel-cal-booked' },
      { label: 'Unavailable', className: 'travel-cal-unavailable' },
    ];
  }
}
