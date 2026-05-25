import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import { TravelTripRequestRow } from 'src/app/core/models/travel.models';
import { TravelEntityPick } from '../../shared/models/travel-entity-pick.model';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { formatTravelDate, parseTravelDate } from '../../shared/utils/travel-date.util';
import { TravelTripRequestService } from '../../services/travel-trip-request.service';

export interface TravelTripRequestFormDialogData {
  row?: TravelTripRequestRow;
}

@Component({
  selector: 'app-travel-trip-request-form-dialog',
  templateUrl: './travel-trip-request-form-dialog.component.html',
})
export class TravelTripRequestFormDialogComponent implements OnInit {
  form!: FormGroup;
  saving = false;
  workflowBusy = false;
  readonly isEdit: boolean;
  ready = false;
  row: TravelTripRequestRow | null = null;

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TravelTripRequestService,
    private readonly lookup: TravelLookupService,
    private readonly ref: MatDialogRef<TravelTripRequestFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TravelTripRequestFormDialogData
  ) {
    this.isEdit = !!data.row?.id;
    this.row = data.row ?? null;
  }

  get status(): string {
    return (this.row?.status ?? 'PENDING').toUpperCase();
  }

  ngOnInit(): void {
    const r = this.data.row;
    this.lookup.ensureCatalog('clients').subscribe({
      next: () => {
        this.form = this.fb.group({
          client: [this.lookup.findPick('client', r?.clientId), Validators.required],
          destinationName: [r?.destinationName ?? ''],
          departureDate: [parseTravelDate(r?.departureDate)],
          returnDate: [parseTravelDate(r?.returnDate)],
          budgetAmount: [r?.budgetAmount ?? null, Validators.min(0)],
          currency: [r?.currency ?? 'USD'],
          travelerCount: [r?.travelerCount ?? 1, Validators.min(1)],
          travelerDetails: [r?.travelerDetails ?? ''],
          notes: [r?.notes ?? ''],
          quotedAmount: [r?.quotedAmount ?? null],
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
    const client = v.client as TravelEntityPick;
    const payload = {
      clientId: client.id,
      destinationName: v.destinationName?.trim() || undefined,
      departureDate: formatTravelDate(v.departureDate),
      returnDate: formatTravelDate(v.returnDate),
      budgetAmount: v.budgetAmount != null ? Number(v.budgetAmount) : undefined,
      currency: v.currency?.trim() || 'USD',
      travelerCount: Number(v.travelerCount),
      travelerDetails: v.travelerDetails?.trim() || undefined,
      notes: v.notes?.trim() || undefined,
    };
    this.saving = true;
    const req$ = this.isEdit
      ? this.api.update({ ...payload, id: this.data.row!.id! })
      : this.api.add(payload);
    req$.pipe(finalize(() => (this.saving = false))).subscribe({
      next: (res) => {
        const created = res as { id?: number };
        if (!this.isEdit && created?.id) {
          this.api.get({ id: created.id }).subscribe({ next: (row) => (this.row = row) });
        }
        this.ref.close(true);
      },
    });
  }

  sendQuote(): void {
    const amt = Number(this.form.get('quotedAmount')?.value);
    if (!this.row?.id || !amt || amt <= 0) {
      alert('Enter a quoted amount first.');
      return;
    }
    this.workflowBusy = true;
    this.api
      .quote({ id: this.row.id, quotedAmount: amt })
      .pipe(finalize(() => (this.workflowBusy = false)))
      .subscribe({
        next: () => this.refreshRow(),
      });
  }

  acceptQuote(): void {
    if (!this.row?.id) {
      return;
    }
    this.workflowBusy = true;
    this.api
      .accept({ id: this.row.id })
      .pipe(finalize(() => (this.workflowBusy = false)))
      .subscribe({ next: () => this.refreshRow() });
  }

  rejectQuote(): void {
    if (!this.row?.id) {
      return;
    }
    this.workflowBusy = true;
    this.api
      .reject({ id: this.row.id })
      .pipe(finalize(() => (this.workflowBusy = false)))
      .subscribe({ next: () => this.refreshRow() });
  }

  convertToBooking(): void {
    if (!this.row?.id) {
      return;
    }
    this.workflowBusy = true;
    this.api
      .convert({ id: this.row.id })
      .pipe(finalize(() => (this.workflowBusy = false)))
      .subscribe({
        next: (res) => {
          alert(`Booking created (ID ${res.bookingId}).`);
          this.ref.close(true);
        },
      });
  }

  private refreshRow(): void {
    if (!this.row?.id) {
      return;
    }
    this.api.get({ id: this.row.id }).subscribe({ next: (r) => (this.row = r) });
  }
}
