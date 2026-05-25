import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Subject, finalize, takeUntil } from 'rxjs';
import { TravelVisaRow } from 'src/app/core/models/travel.models';
import { TravelEntityPick } from '../../shared/models/travel-entity-pick.model';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { TRAVEL_VISA_STATUSES, TRAVEL_VISA_TYPES } from '../../shared/travel-ui.constants';
import { combineTravelDateAndTime, parseTravelDateTime } from '../../shared/utils/travel-datetime.util';
import { TravelVisaService } from '../../services/travel-visa.service';
import { TravelClientService } from '../../services/travel-client.service';
import { TravelPassportPreviewData } from '../../shared/components/passport-preview/travel-passport-preview.component';

export interface TravelVisaFormDialogData {
  row?: TravelVisaRow;
}

@Component({
  selector: 'app-travel-visa-form-dialog',
  templateUrl: './travel-visa-form-dialog.component.html',
})
export class TravelVisaFormDialogComponent implements OnInit, OnDestroy {
  form!: FormGroup;
  saving = false;
  readonly isEdit: boolean;
  readonly statusOptions = [...TRAVEL_VISA_STATUSES];
  readonly visaTypes = [...TRAVEL_VISA_TYPES];
  ready = false;
  passportPreview: TravelPassportPreviewData = {};
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TravelVisaService,
    private readonly clients: TravelClientService,
    private readonly lookup: TravelLookupService,
    private readonly ref: MatDialogRef<TravelVisaFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TravelVisaFormDialogData
  ) {
    this.isEdit = !!data.row?.id;
  }

  ngOnInit(): void {
    const r = this.data.row;
    const sub = parseTravelDateTime(r?.submittedAt);
    const dec = parseTravelDateTime(r?.decisionAt);
    this.lookup.ensureCatalog('clients', 'bookings').subscribe({
      next: () => {
        this.form = this.fb.group({
          client: [this.lookup.findPick('client', r?.clientId), Validators.required],
          booking: [this.lookup.findPick('booking', r?.bookingId)],
          country: [r?.country ?? '', Validators.required],
          visaType: [r?.visaType ?? 'TOURIST'],
          status: [r?.status ?? 'PENDING'],
          submittedDate: [sub.date],
          submittedTime: [sub.timeHHmm],
          decisionDate: [dec.date],
          decisionTime: [dec.timeHHmm],
          notes: [r?.notes ?? ''],
        });
        this.form
          .get('client')
          ?.valueChanges.pipe(takeUntil(this.destroy$))
          .subscribe(() => this.loadTravelerProfile());
        this.form
          .get('country')
          ?.valueChanges.pipe(takeUntil(this.destroy$))
          .subscribe(() => this.patchCountryOnPreview());
        this.ready = true;
        this.loadTravelerProfile();
      },
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  get filterClientId(): number | null {
    const c = this.form?.get('client')?.value as TravelEntityPick | null;
    return c?.id ?? null;
  }

  get visaStatusClass(): string {
    return (this.form?.get('status')?.value ?? 'pending').toString().toLowerCase();
  }

  loadTravelerProfile(): void {
    const c = this.form?.get('client')?.value as TravelEntityPick | null;
    if (!c?.id) {
      this.passportPreview = {};
      return;
    }
    this.clients.get({ id: c.id }).subscribe({
      next: (client) => {
        const p = client.travelProfile;
        this.passportPreview = {
          fullName: client.fullName,
          nationality: p?.nationality,
          passportNo: client.passportNo,
          passportIssueDate: p?.passportIssueDate,
          passportExpiryDate: p?.passportExpiryDate,
          passportPlaceOfIssue: p?.passportPlaceOfIssue,
          passportType: p?.passportType,
          nationalId: p?.nationalId,
          countryCode: this.form.get('country')?.value || p?.nationality,
        };
      },
    });
  }

  patchCountryOnPreview(): void {
    this.passportPreview = {
      ...this.passportPreview,
      countryCode: this.form.get('country')?.value,
    };
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
    const booking = v.booking as TravelEntityPick | null;
    const payload = {
      clientId: client.id,
      bookingId: booking?.id,
      country: String(v.country).trim(),
      visaType: v.visaType,
      status: v.status,
      submittedAt:
        v.submittedDate instanceof Date
          ? combineTravelDateAndTime(v.submittedDate, v.submittedTime)
          : undefined,
      decisionAt:
        v.decisionDate instanceof Date ? combineTravelDateAndTime(v.decisionDate, v.decisionTime) : undefined,
      notes: v.notes?.trim() || undefined,
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
