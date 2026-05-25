import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import { TravelSupplierRow } from 'src/app/core/models/travel.models';
import { TRAVEL_SUPPLIER_TYPES } from '../../shared/travel-ui.constants';
import { TravelSupplierService } from '../../services/travel-supplier.service';

export interface TravelSupplierFormDialogData {
  row?: TravelSupplierRow;
}

@Component({
  selector: 'app-travel-supplier-form-dialog',
  templateUrl: './travel-supplier-form-dialog.component.html',
})
export class TravelSupplierFormDialogComponent implements OnInit {
  form!: FormGroup;
  saving = false;
  readonly isEdit: boolean;
  readonly supplierTypes = [...TRAVEL_SUPPLIER_TYPES];

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TravelSupplierService,
    private readonly ref: MatDialogRef<TravelSupplierFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TravelSupplierFormDialogData
  ) {
    this.isEdit = !!data.row?.id;
  }

  ngOnInit(): void {
    const r = this.data.row;
    this.form = this.fb.group({
      name: [r?.name ?? '', Validators.required],
      supplierType: [r?.supplierType ?? ''],
      contactEmail: [r?.contactEmail ?? ''],
      contactPhone: [r?.contactPhone ?? ''],
      contractsJson: [r?.contractsJson ?? ''],
      commissionNotes: [r?.commissionNotes ?? ''],
      rateTableNotes: [r?.rateTableNotes ?? ''],
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
      supplierType: v.supplierType?.trim() || undefined,
      contactEmail: v.contactEmail?.trim() || undefined,
      contactPhone: v.contactPhone?.trim() || undefined,
      contractsJson: v.contractsJson?.trim() || undefined,
      commissionNotes: v.commissionNotes?.trim() || undefined,
      rateTableNotes: v.rateTableNotes?.trim() || undefined,
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
