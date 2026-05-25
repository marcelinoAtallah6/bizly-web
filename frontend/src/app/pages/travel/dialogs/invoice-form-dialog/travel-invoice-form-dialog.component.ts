import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import { TravelInvoiceRow } from 'src/app/core/models/travel.models';
import { TravelEntityPick } from '../../shared/models/travel-entity-pick.model';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { TRAVEL_INVOICE_STATUSES } from '../../shared/travel-ui.constants';
import { combineTravelDateAndTime, parseTravelDateTime } from '../../shared/utils/travel-datetime.util';
import { TravelInvoiceService } from '../../services/travel-invoice.service';

export interface TravelInvoiceFormDialogData {
  row?: TravelInvoiceRow;
}

@Component({
  selector: 'app-travel-invoice-form-dialog',
  templateUrl: './travel-invoice-form-dialog.component.html',
})
export class TravelInvoiceFormDialogComponent implements OnInit {
  form!: FormGroup;
  saving = false;
  readonly isEdit: boolean;
  readonly statusOptions = [...TRAVEL_INVOICE_STATUSES];
  ready = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TravelInvoiceService,
    private readonly lookup: TravelLookupService,
    private readonly ref: MatDialogRef<TravelInvoiceFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TravelInvoiceFormDialogData
  ) {
    this.isEdit = !!data.row?.id;
  }

  ngOnInit(): void {
    const r = this.data.row;
    const issued = parseTravelDateTime(r?.issuedAt);
    const due = parseTravelDateTime(r?.dueAt);
    this.lookup.ensureCatalog('bookings').subscribe({
      next: () => {
        this.form = this.fb.group({
          booking: [this.lookup.findPick('booking', r?.bookingId), Validators.required],
          invoiceNo: [r?.invoiceNo ?? ''],
          amount: [r?.amount ?? 0, [Validators.required, Validators.min(0)]],
          currency: [r?.currency ?? 'USD'],
          status: [r?.status ?? 'DRAFT'],
          issuedDate: [issued.date],
          issuedTime: [issued.timeHHmm],
          dueDate: [due.date],
          dueTime: [due.timeHHmm],
        });
        this.ready = true;
      },
    });
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
    const booking = v.booking as TravelEntityPick;
    const payload = {
      bookingId: booking.id,
      invoiceNo: v.invoiceNo?.trim() || undefined,
      amount: Number(v.amount),
      currency: v.currency?.trim() || 'USD',
      status: v.status,
      issuedAt: v.issuedDate instanceof Date ? combineTravelDateAndTime(v.issuedDate, v.issuedTime) : undefined,
      dueAt: v.dueDate instanceof Date ? combineTravelDateAndTime(v.dueDate, v.dueTime) : undefined,
    };
    this.saving = true;
    const req$ = this.isEdit
      ? this.api.update({ ...payload, id: this.data.row!.id! })
      : this.api.add(payload);
    req$.pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => this.ref.close(true),
    });
  }
}
