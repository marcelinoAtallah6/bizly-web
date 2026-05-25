import { Component, Inject, OnInit, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatStepper } from '@angular/material/stepper';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import { TravelBookingRow } from 'src/app/core/models/travel.models';
import { TravelEntityPick } from '../../shared/models/travel-entity-pick.model';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import {
  TRAVEL_APPROVAL_STATUSES,
  TRAVEL_BOOKING_STATUSES,
  TRAVEL_TIMELINE_STAGES,
} from '../../shared/travel-ui.constants';
import { formatTravelDate, parseTravelDate } from '../../shared/utils/travel-date.util';
import { TravelBookingService } from '../../services/travel-booking.service';

export interface TravelBookingFormDialogData {
  row?: TravelBookingRow;
}

@Component({
  selector: 'app-travel-booking-form-dialog',
  templateUrl: './travel-booking-form-dialog.component.html',
  styleUrl: './travel-booking-form-dialog.component.scss',
})
export class TravelBookingFormDialogComponent implements OnInit {
  form!: FormGroup;
  saving = false;
  readonly isEdit: boolean;
  readonly statusOptions = [...TRAVEL_BOOKING_STATUSES];
  readonly timelineStages = [...TRAVEL_TIMELINE_STAGES];
  readonly approvalStatuses = [...TRAVEL_APPROVAL_STATUSES];
  ready = false;
  /** Stepper has 4 steps (indices 0–3); avoids protected `MatStepper.steps` in template. */
  readonly bookingLastStepIndex = 3;

  @ViewChild('bookingStepper') bookingStepper?: MatStepper;

  get bookingBackDisabled(): boolean {
    return (this.bookingStepper?.selectedIndex ?? 0) === 0;
  }

  get bookingNextDisabled(): boolean {
    return (this.bookingStepper?.selectedIndex ?? 0) >= this.bookingLastStepIndex;
  }

  get paymentSchedule(): FormArray {
    return this.form.get('paymentSchedule') as FormArray;
  }

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TravelBookingService,
    private readonly lookup: TravelLookupService,
    private readonly ref: MatDialogRef<TravelBookingFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TravelBookingFormDialogData
  ) {
    this.isEdit = !!data.row?.id;
  }

  ngOnInit(): void {
    const r = this.data.row;
    this.lookup.ensureCatalog('clients', 'packages', 'bookings').subscribe({
      next: () => {
        this.form = this.fb.group({
          client: [this.lookup.findPick('client', r?.clientId), Validators.required],
          package: [this.lookup.findPick('package', r?.packageId)],
          referenceNo: [r?.referenceNo ?? ''],
          status: [r?.status ?? 'ENQUIRY', Validators.required],
          departureDate: [parseTravelDate(r?.departureDate)],
          returnDate: [parseTravelDate(r?.returnDate)],
          totalAmount: [r?.totalAmount ?? 0, [Validators.min(0)]],
          currency: [r?.currency ?? 'USD'],
          notes: [r?.notes ?? ''],
          timelineStage: [r?.timelineStage ?? 'QUOTATION'],
          approvalStatus: [r?.approvalStatus ?? 'NOT_REQUIRED'],
          requiresApproval: [r?.requiresApproval === true],
          paymentSchedule: this.fb.array([]),
        });
        for (const item of r?.paymentSchedule ?? []) {
          this.addPaymentItem(item);
        }
        this.ready = true;
      },
    });
  }

  addPaymentItem(item?: { dueDate?: string; amount?: number; label?: string; paid?: boolean }): void {
    this.paymentSchedule.push(
      this.fb.group({
        label: [item?.label ?? 'Installment'],
        amount: [item?.amount ?? 0, Validators.min(0)],
        dueDate: [parseTravelDate(item?.dueDate)],
        paid: [item?.paid === true],
      })
    );
  }

  removePaymentItem(index: number): void {
    this.paymentSchedule.removeAt(index);
  }

  cancel(): void {
    this.ref.close(false);
  }

  save(): void {
    if (!this.form || this.form.invalid) {
      this.form?.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const client = v.client as TravelEntityPick | null;
    const pkg = v.package as TravelEntityPick | null;
    const payload = {
      clientId: client!.id,
      packageId: pkg?.id,
      referenceNo: v.referenceNo?.trim() || undefined,
      status: v.status,
      departureDate: formatTravelDate(v.departureDate),
      returnDate: formatTravelDate(v.returnDate),
      totalAmount: v.totalAmount != null ? Number(v.totalAmount) : undefined,
      currency: v.currency?.trim() || 'USD',
      notes: v.notes?.trim() || undefined,
      timelineStage: v.timelineStage,
      approvalStatus: v.approvalStatus,
      requiresApproval: !!v.requiresApproval,
      paymentSchedule: (v.paymentSchedule ?? []).map(
        (p: { label?: string; amount?: number; dueDate?: Date; paid?: boolean }) => ({
          label: p.label?.trim() || undefined,
          amount: p.amount != null ? Number(p.amount) : undefined,
          dueDate: formatTravelDate(p.dueDate),
          paid: !!p.paid,
        })
      ),
    };
    this.saving = true;
    const req$ = this.isEdit
      ? this.api.update({ ...payload, id: this.data.row!.id! })
      : this.api.add(payload);
    req$.pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => {
        this.lookup.invalidate();
        this.ref.close(true);
      },
    });
  }
}
