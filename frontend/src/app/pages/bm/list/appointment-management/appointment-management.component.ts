import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import { CalendarAppointmentDto } from 'src/app/core/models/bm.models';
import { MenuPermissionService } from 'src/app/services/menu-permission.service';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import {
  AppointmentFormDialogComponent,
  AppointmentFormDialogData,
} from '../../dialogs/appointment-form-dialog/appointment-form-dialog.component';
import { BmAppointmentService } from '../../services/bm-appointment.service';

interface DayGroup {
  dayKey: string;
  label: string;
  items: CalendarAppointmentDto[];
}

@Component({
  selector: 'app-appointment-management',
  templateUrl: './appointment-management.component.html',
  styleUrl: './appointment-management.component.scss',
})
export class AppointmentManagementComponent implements OnInit {
  appointments: CalendarAppointmentDto[] = [];
  dayGroups: DayGroup[] = [];
  loading = false;

  /** First day of the visible month (local) */
  viewMonth = this.startOfMonth(new Date());

  selected: CalendarAppointmentDto | null = null;

  readonly routeAppts = '/bm/appointments';

  constructor(
    private readonly api: BmAppointmentService,
    private readonly dialog: MatDialog,
    readonly menuPerm: MenuPermissionService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  get listToolbar(): ToolbarButton[] {
    const canAdd = this.menuPerm.can(this.routeAppts, 'add');
    const canEdit = this.menuPerm.can(this.routeAppts, 'edit');
    const canCancel = this.menuPerm.can(this.routeAppts, 'delete');
    return [
      {
        id: 'refresh',
        icon: 'refresh',
        tooltip: 'Refresh',
        action: () => this.load(),
        disabled: this.loading,
      },
      {
        id: 'add',
        icon: 'event_available',
        tooltip: 'New appointment',
        action: () => this.openCreate(),
        color: 'primary',
        hidden: !canAdd,
        disabled: this.loading,
      },
      {
        id: 'edit',
        icon: 'edit',
        tooltip: 'Edit selected',
        action: () => this.openEdit(),
        hidden: !canEdit,
        disabled: this.loading || !this.selected,
      },
      {
        id: 'cancel',
        icon: 'event_busy',
        tooltip: 'Cancel selected',
        action: () => this.cancelSelected(),
        hidden: !canCancel,
        disabled: this.loading || !this.selected,
      },
    ];
  }

  load(): void {
    const y = this.viewMonth.getFullYear();
    const m = String(this.viewMonth.getMonth() + 1).padStart(2, '0');
    const monthStr = `${y}-${m}`;
    this.loading = true;
    this.api
      .calendar({ month: monthStr })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (rows) => {
          this.appointments = rows ?? [];
          this.selected = null;
          this.dayGroups = this.buildDayGroups(this.appointments);
        },
        error: () => {},
      });
  }

  prevMonth(): void {
    this.viewMonth = new Date(this.viewMonth.getFullYear(), this.viewMonth.getMonth() - 1, 1);
    this.load();
  }

  nextMonth(): void {
    this.viewMonth = new Date(this.viewMonth.getFullYear(), this.viewMonth.getMonth() + 1, 1);
    this.load();
  }

  goTodayMonth(): void {
    this.viewMonth = this.startOfMonth(new Date());
    this.load();
  }

  selectCard(a: CalendarAppointmentDto): void {
    this.selected = a;
  }

  /** Double-click on a card opens the edit dialog only when the role has edit on Appointments. */
  onCardDoubleClick(): void {
    if (!this.menuPerm.can(this.routeAppts, 'edit')) {
      return;
    }
    this.openEdit();
  }

  trackAppt(_i: number, a: CalendarAppointmentDto): number {
    return a.id;
  }

  openCreate(): void {
    const ref = this.dialog.open<AppointmentFormDialogComponent, AppointmentFormDialogData, boolean>(
      AppointmentFormDialogComponent,
      {
        width: '580px',
        maxWidth: '96vw',
        data: { mode: 'create' },
      }
    );
    ref.afterClosed().subscribe((ok) => {
      if (ok) {
        this.load();
      }
    });
  }

  openEdit(): void {
    if (!this.selected) {
      return;
    }
    const ref = this.dialog.open<AppointmentFormDialogComponent, AppointmentFormDialogData, boolean>(
      AppointmentFormDialogComponent,
      {
        width: '580px',
        maxWidth: '96vw',
        data: { mode: 'edit', row: this.selected },
      }
    );
    ref.afterClosed().subscribe((ok) => {
      if (ok) {
        this.load();
      }
    });
  }

  cancelSelected(): void {
    const row = this.selected;
    if (!row?.id) {
      return;
    }
    if (!confirm(`Cancel appointment "${row.title}"?`)) {
      return;
    }
    this.loading = true;
    this.api
      .cancel({ id: row.id })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: () => this.load(),
        error: () => {},
      });
  }

  cardColorClass(color: string): string {
    switch (color) {
      case 'green':
        return 'bm-strip--green';
      case 'red':
        return 'bm-strip--red';
      case 'yellow':
        return 'bm-strip--amber';
      case 'blue':
        return 'bm-strip--blue';
      default:
        return 'bm-strip--muted';
    }
  }

  private startOfMonth(d: Date): Date {
    return new Date(d.getFullYear(), d.getMonth(), 1);
  }

  private buildDayGroups(rows: CalendarAppointmentDto[]): DayGroup[] {
    const map = new Map<string, CalendarAppointmentDto[]>();
    for (const r of rows) {
      const dayKey = (r.startTime ?? '').substring(0, 10);
      if (!dayKey) {
        continue;
      }
      const list = map.get(dayKey) ?? [];
      list.push(r);
      map.set(dayKey, list);
    }
    const keys = [...map.keys()].sort();
    return keys.map((dayKey) => {
      const items = (map.get(dayKey) ?? []).sort((a, b) =>
        (a.startTime ?? '').localeCompare(b.startTime ?? '')
      );
      const label = this.formatDayHeading(dayKey);
      return { dayKey, label, items };
    });
  }

  private formatDayHeading(isoDate: string): string {
    const [y, m, d] = isoDate.split('-').map((x) => parseInt(x, 10));
    if (!y || !m || !d) {
      return isoDate;
    }
    const dt = new Date(y, m - 1, d);
    const weekday = dt.toLocaleDateString(undefined, { weekday: 'long' });
    const rest = dt.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
    return `${weekday} · ${rest}`;
  }
}
