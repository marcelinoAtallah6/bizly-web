import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import {
  AppointmentStatus,
  CalendarAppointmentDto,
  GetServiceItemResponse,
} from 'src/app/core/models/bm.models';
import { GetCustomerResponse as KycCustomer } from 'src/app/core/models/kyc.models';
import { KycCustomerService } from 'src/app/pages/kyc/services/kyc-customer.service';
import { apiDateTimeToDateAndTime, combineDateAndTimeToApi } from '../../utils/bm-datetime';
import { BmAppointmentService } from '../../services/bm-appointment.service';
import { BmServiceItemService } from '../../services/bm-service-item.service';

export interface AppointmentFormDialogData {
  mode: 'create' | 'edit';
  /** Existing row from calendar */
  row?: CalendarAppointmentDto;
}

@Component({
  selector: 'app-appointment-form-dialog',
  templateUrl: './appointment-form-dialog.component.html',
  styleUrl: './appointment-form-dialog.component.scss',
})
export class AppointmentFormDialogComponent implements OnInit {
  saving = false;
  loading = true;

  /** Normalized id for load/update; avoids relying on calendar row shape alone */
  editAppointmentId: number | null = null;

  /** Shown in edit mode instead of customer dropdown (customer control stays valid but hidden) */
  editCustomerLabel = '';

  customers: KycCustomer[] = [];
  services: GetServiceItemResponse[] = [];

  statuses: AppointmentStatus[] = ['PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'];

  /** Mat-select compareWith for numeric ids from API */
  compareIds = (a: unknown, b: unknown): boolean =>
    a != null && b != null && Number(a) === Number(b);

  form = this.fb.group({
    customerId: [null as number | null, Validators.required],
    serviceId: [null as number | null, Validators.required],
    title: ['', [Validators.required, Validators.maxLength(500)]],
    startDate: [null as Date | null, Validators.required],
    startTimeOnly: ['09:00', Validators.required],
    notes: [''],
    status: ['PENDING' as AppointmentStatus, Validators.required],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly dialogRef: MatDialogRef<AppointmentFormDialogComponent, boolean>,
    private readonly appointmentApi: BmAppointmentService,
    private readonly serviceItemApi: BmServiceItemService,
    private readonly kycCustomerApi: KycCustomerService,
    private readonly snack: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public readonly data: AppointmentFormDialogData
  ) {}

  ngOnInit(): void {
    const raw = this.data.row?.id;
    const n = raw != null ? Number(raw) : NaN;
    this.editAppointmentId = Number.isFinite(n) && n > 0 ? n : null;

    forkJoin({
      cust: this.kycCustomerApi.gets({ pageNumber: 0, pageSize: 500 }).pipe(
        catchError(() => of({ items: [] as KycCustomer[] }))
      ),
      svc: this.serviceItemApi
        .gets({
          pageNumber: 0,
          pageSize: 1000,
          /** Include inactive so edit can keep the booked service in the dropdown */
          includeInactive: this.data.mode === 'edit',
        })
        .pipe(catchError(() => of({ items: [] as GetServiceItemResponse[] }))),
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: ({ cust, svc }) => {
          this.customers = cust.items ?? [];
          this.services =
            this.data.mode === 'edit'
              ? (svc.items ?? [])
              : (svc.items ?? []).filter((s) => s.active !== false);
          if (this.data.mode === 'edit' && this.editAppointmentId != null) {
            this.loadAppointment(this.editAppointmentId);
          } else {
            if (this.data.mode === 'edit' && this.editAppointmentId == null) {
              this.snack.open('Cannot edit this appointment (missing id).', 'Dismiss', { duration: 5000 });
            }
            this.patchDefaultStartFromNow();
          }
        },
      });
  }

  /** Default date/time when creating */
  private patchDefaultStartFromNow(): void {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    this.form.patchValue({
      startDate: new Date(now.getFullYear(), now.getMonth(), now.getDate()),
      startTimeOnly: `${hh}:${mm}`,
    });
  }

  private loadAppointment(id: number): void {
    this.loading = true;
    this.appointmentApi
      .get({ id })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (a) => {
          const { date, timeHHmm } = apiDateTimeToDateAndTime(a.startTime);
          this.editCustomerLabel =
            (a.customerName && String(a.customerName).trim()) ||
            this.customerLabelFromId(a.customerId) ||
            `Customer #${a.customerId}`;

          if (a.service && !this.services.some((s) => s.id === a.service?.id)) {
            this.services = [...this.services, a.service];
          }

          this.form.patchValue({
            customerId: a.customerId,
            serviceId: a.serviceId,
            title: a.title,
            startDate: date,
            startTimeOnly: timeHHmm,
            notes: a.notes ?? '',
            status: a.status,
          });
        },
        error: () => {
          this.snack.open('Could not load appointment for editing.', 'Dismiss', { duration: 6000 });
        },
      });
  }

  private customerLabelFromId(customerId: number | null | undefined): string {
    if (customerId == null) {
      return '';
    }
    const c = this.customers.find((x) => x.id === customerId);
    return c ? this.customerLabel(c) : '';
  }

  submit(): void {
    if (this.saving) {
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const d = v.startDate as Date | null;
    const start =
      d && v.startTimeOnly ? combineDateAndTimeToApi(d, v.startTimeOnly as string) : '';
    if (!start) {
      return;
    }

    if (this.data.mode === 'edit' && this.editAppointmentId == null) {
      this.snack.open('Cannot save: appointment id is missing.', 'Dismiss', { duration: 5000 });
      return;
    }

    this.saving = true;

    if (this.data.mode === 'create') {
      this.appointmentApi
        .add({
          customerId: v.customerId!,
          serviceId: v.serviceId!,
          title: v.title!,
          startTime: start,
          notes: v.notes?.trim() ? v.notes.trim() : undefined,
          status: v.status ?? 'PENDING',
        })
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: () => this.dialogRef.close(true),
          error: () => {},
        });
    } else if (this.editAppointmentId != null) {
      this.appointmentApi
        .update({
          id: this.editAppointmentId,
          serviceId: v.serviceId ?? undefined,
          title: v.title ?? undefined,
          startTime: start,
          notes: v.notes ?? undefined,
          status: v.status ?? undefined,
        })
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: () => this.dialogRef.close(true),
          error: () => {},
        });
    } else {
      this.saving = false;
    }
  }

  cancel(): void {
    this.dialogRef.close(false);
  }

  customerLabel(c: KycCustomer): string {
    if (c.fullName?.trim()) {
      return c.fullName.trim();
    }
    const n = `${c.firstName ?? ''} ${c.lastName ?? ''}`.trim();
    return n || `#${c.id}`;
  }
}
