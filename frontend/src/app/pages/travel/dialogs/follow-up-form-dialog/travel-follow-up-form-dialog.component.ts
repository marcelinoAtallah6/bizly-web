import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import { GetUserResponse } from 'src/app/core/models/um.models';
import { TravelFollowUpRow } from 'src/app/core/models/travel.models';
import { TravelEntityPick } from '../../shared/models/travel-entity-pick.model';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { TRAVEL_FOLLOW_UP_STATUSES } from '../../shared/travel-ui.constants';
import { combineTravelDateAndTime, parseTravelDateTime } from '../../shared/utils/travel-datetime.util';
import { TravelFollowUpService } from '../../services/travel-follow-up.service';

export interface TravelFollowUpFormDialogData {
  row?: TravelFollowUpRow;
}

@Component({
  selector: 'app-travel-follow-up-form-dialog',
  templateUrl: './travel-follow-up-form-dialog.component.html',
})
export class TravelFollowUpFormDialogComponent implements OnInit {
  form!: FormGroup;
  saving = false;
  readonly isEdit: boolean;
  readonly statusOptions = [...TRAVEL_FOLLOW_UP_STATUSES];
  ready = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TravelFollowUpService,
    private readonly lookup: TravelLookupService,
    private readonly ref: MatDialogRef<TravelFollowUpFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TravelFollowUpFormDialogData
  ) {
    this.isEdit = !!data.row?.id;
  }

  ngOnInit(): void {
    const r = this.data.row;
    const due = parseTravelDateTime(r?.dueAt);
    this.lookup.ensureCatalog('clients', 'bookings', 'users').subscribe({
      next: () => {
        let consultant: TravelEntityPick | null = null;
        if (r?.assignedTo) {
          const u = this.lookup.picksFor('user').find(
            (p) =>
              p.primaryLabel === r.assignedTo ||
              (p.raw as GetUserResponse).username === r.assignedTo
          );
          consultant = u ?? null;
        }
        this.form = this.fb.group({
          client: [this.lookup.findPick('client', r?.clientId)],
          booking: [this.lookup.findPick('booking', r?.bookingId)],
          subject: [r?.subject ?? '', Validators.required],
          dueDate: [due.date],
          dueTime: [due.timeHHmm],
          status: [r?.status ?? 'OPEN'],
          consultant: [consultant],
          notes: [r?.notes ?? ''],
        });
        this.ready = true;
      },
    });
  }

  get filterClientId(): number | null {
    const c = this.form?.get('client')?.value as TravelEntityPick | null;
    return c?.id ?? null;
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
    const booking = v.booking as TravelEntityPick | null;
    const consultant = v.consultant as TravelEntityPick | null;
    const payload = {
      clientId: client?.id,
      bookingId: booking?.id,
      subject: String(v.subject).trim(),
      dueAt: v.dueDate instanceof Date ? combineTravelDateAndTime(v.dueDate, v.dueTime) : undefined,
      status: v.status,
      assignedTo: consultant?.primaryLabel,
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
