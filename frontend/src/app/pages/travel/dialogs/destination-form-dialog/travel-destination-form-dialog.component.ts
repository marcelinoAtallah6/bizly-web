import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import { TravelDestinationRow } from 'src/app/core/models/travel.models';
import { TRAVEL_RISK_LEVELS } from '../../shared/travel-ui.constants';
import { TravelDestinationService } from '../../services/travel-destination.service';

export interface TravelDestinationFormDialogData {
  row?: TravelDestinationRow;
}

@Component({
  selector: 'app-travel-destination-form-dialog',
  templateUrl: './travel-destination-form-dialog.component.html',
})
export class TravelDestinationFormDialogComponent implements OnInit {
  form!: FormGroup;
  saving = false;
  readonly isEdit: boolean;
  readonly riskLevels = [...TRAVEL_RISK_LEVELS];

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TravelDestinationService,
    private readonly ref: MatDialogRef<TravelDestinationFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TravelDestinationFormDialogData
  ) {
    this.isEdit = !!data.row?.id;
  }

  ngOnInit(): void {
    const r = this.data.row;
    this.form = this.fb.group({
      name: [r?.name ?? '', Validators.required],
      country: [r?.country ?? ''],
      region: [r?.region ?? ''],
      visaRequirements: [r?.visaRequirements ?? ''],
      healthRequirements: [r?.healthRequirements ?? ''],
      highSeasonNotes: [r?.highSeasonNotes ?? ''],
      lowSeasonNotes: [r?.lowSeasonNotes ?? ''],
      travelWarnings: [r?.travelWarnings ?? ''],
      riskLevel: [r?.riskLevel ?? 'LOW'],
      travelAdvisory: [r?.travelAdvisory ?? ''],
      active: [r?.active !== false],
    });
  }

  cancel(): void {
    this.ref.close(false);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const payload = {
      name: String(v.name).trim(),
      country: v.country?.trim() || undefined,
      region: v.region?.trim() || undefined,
      visaRequirements: v.visaRequirements?.trim() || undefined,
      healthRequirements: v.healthRequirements?.trim() || undefined,
      highSeasonNotes: v.highSeasonNotes?.trim() || undefined,
      lowSeasonNotes: v.lowSeasonNotes?.trim() || undefined,
      travelWarnings: v.travelWarnings?.trim() || undefined,
      riskLevel: v.riskLevel,
      travelAdvisory: v.travelAdvisory?.trim() || undefined,
      active: !!v.active,
    };
    this.saving = true;
    const req$ = this.isEdit
      ? this.api.update({ ...payload, id: this.data.row!.id! })
      : this.api.add(payload);
    req$.pipe(finalize(() => (this.saving = false))).subscribe({ next: () => this.ref.close(true) });
  }
}
