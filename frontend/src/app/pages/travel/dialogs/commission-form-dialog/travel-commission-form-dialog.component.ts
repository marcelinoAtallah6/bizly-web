import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import { TravelCommissionRuleRow } from 'src/app/core/models/travel.models';
import { TRAVEL_COMMISSION_RULE_TYPES } from '../../shared/travel-ui.constants';
import { TravelCommissionService } from '../../services/travel-commission.service';

export interface TravelCommissionFormDialogData {
  row?: TravelCommissionRuleRow;
}

@Component({
  selector: 'app-travel-commission-form-dialog',
  templateUrl: './travel-commission-form-dialog.component.html',
})
export class TravelCommissionFormDialogComponent implements OnInit {
  form!: FormGroup;
  saving = false;
  readonly isEdit: boolean;
  readonly ruleTypes = [...TRAVEL_COMMISSION_RULE_TYPES];

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TravelCommissionService,
    private readonly ref: MatDialogRef<TravelCommissionFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TravelCommissionFormDialogData
  ) {
    this.isEdit = !!data.row?.id;
  }

  ngOnInit(): void {
    const r = this.data.row;
    this.form = this.fb.group({
      name: [r?.name ?? '', Validators.required],
      ruleType: [r?.ruleType ?? 'PERCENT', Validators.required],
      ratePercent: [r?.ratePercent ?? null],
      flatAmount: [r?.flatAmount ?? null],
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
      ruleType: String(v.ruleType).trim(),
      ratePercent: v.ratePercent != null ? Number(v.ratePercent) : undefined,
      flatAmount: v.flatAmount != null ? Number(v.flatAmount) : undefined,
      active: !!v.active,
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
