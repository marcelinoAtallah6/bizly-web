import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import { GetServiceItemResponse } from 'src/app/core/models/bm.models';
import { isWorkflowDeferredResult } from 'src/app/services/business-api.service';
import { BmServiceItemService } from '../../services/bm-service-item.service';

export interface ServiceFormDialogData {
  mode: 'create' | 'edit';
  row?: GetServiceItemResponse;
}

@Component({
  selector: 'app-service-form-dialog',
  templateUrl: './service-form-dialog.component.html',
  styleUrl: './service-form-dialog.component.scss',
})
export class ServiceFormDialogComponent implements OnInit {
  saving = false;

  form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(300)]],
    description: [''],
    price: [0, [Validators.required, Validators.min(0)]],
    durationMinutes: [30, [Validators.required, Validators.min(1)]],
    active: [true],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly dialogRef: MatDialogRef<ServiceFormDialogComponent, boolean>,
    private readonly serviceItemApi: BmServiceItemService,
    @Inject(MAT_DIALOG_DATA) public readonly data: ServiceFormDialogData
  ) {}

  ngOnInit(): void {
    if (this.data.mode === 'edit' && this.data.row) {
      const r = this.data.row;
      this.form.patchValue({
        name: r.name,
        description: r.description ?? '',
        price: r.price,
        durationMinutes: r.durationMinutes,
        active: r.active,
      });
    }
  }

  submit(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.saving = true;

    if (this.data.mode === 'create') {
      this.serviceItemApi
        .add({
          name: v.name!,
          description: v.description || undefined,
          price: v.price!,
          durationMinutes: v.durationMinutes!,
          active: v.active ?? true,
        })
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: (res) => this.dialogRef.close(!isWorkflowDeferredResult(res)),
          error: () => {},
        });
    } else if (this.data.row?.id != null) {
      this.serviceItemApi
        .update({
          id: this.data.row.id,
          name: v.name!,
          description: v.description || undefined,
          price: v.price!,
          durationMinutes: v.durationMinutes!,
          active: v.active ?? true,
        })
        .pipe(finalize(() => (this.saving = false)))
        .subscribe({
          next: (res) => this.dialogRef.close(!isWorkflowDeferredResult(res)),
          error: () => {},
        });
    }
  }

  cancel(): void {
    this.dialogRef.close(false);
  }
}
