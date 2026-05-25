import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import { TravelPaymentRow } from 'src/app/core/models/travel.models';
import { TravelEntityPick } from '../../shared/models/travel-entity-pick.model';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { TRAVEL_PAYMENT_METHODS } from '../../shared/travel-ui.constants';
import { TravelPaymentService } from '../../services/travel-payment.service';

export interface TravelPaymentFormDialogData {
  row?: TravelPaymentRow;
}

@Component({
  selector: 'app-travel-payment-form-dialog',
  templateUrl: './travel-payment-form-dialog.component.html',
})
export class TravelPaymentFormDialogComponent implements OnInit {
  form!: FormGroup;
  saving = false;
  readonly isEdit: boolean;
  readonly paymentMethods = [...TRAVEL_PAYMENT_METHODS];
  ready = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TravelPaymentService,
    private readonly lookup: TravelLookupService,
    private readonly ref: MatDialogRef<TravelPaymentFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TravelPaymentFormDialogData
  ) {
    this.isEdit = !!data.row?.id;
  }

  ngOnInit(): void {
    const r = this.data.row;
    this.lookup.ensureCatalog('invoices').subscribe({
      next: () => {
        this.form = this.fb.group({
          invoice: [this.lookup.findPick('invoice', r?.invoiceId), Validators.required],
          amount: [r?.amount ?? 0, [Validators.required, Validators.min(0)]],
          paymentMethod: [r?.paymentMethod ?? 'BANK_TRANSFER'],
          referenceNo: [r?.referenceNo ?? ''],
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
    const invoice = v.invoice as TravelEntityPick;
    const payload = {
      invoiceId: invoice.id,
      amount: Number(v.amount),
      paymentMethod: v.paymentMethod,
      referenceNo: v.referenceNo?.trim() || undefined,
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
