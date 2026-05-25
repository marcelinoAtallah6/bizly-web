import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize, switchMap } from 'rxjs';
import { TravelDocumentRow } from 'src/app/core/models/travel.models';
import { TravelEntityPick } from '../../shared/models/travel-entity-pick.model';
import { TravelLookupService } from '../../shared/services/travel-lookup.service';
import { TRAVEL_DOCUMENT_TYPES } from '../../shared/travel-ui.constants';
import { TravelDocumentService } from '../../services/travel-document.service';

export interface TravelDocumentFormDialogData {
  row?: TravelDocumentRow;
}

@Component({
  selector: 'app-travel-document-form-dialog',
  templateUrl: './travel-document-form-dialog.component.html',
})
export class TravelDocumentFormDialogComponent implements OnInit {
  form!: FormGroup;
  saving = false;
  readonly isEdit: boolean;
  readonly docTypes = [...TRAVEL_DOCUMENT_TYPES];
  ready = false;
  selectedFile: File | null = null;

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TravelDocumentService,
    private readonly lookup: TravelLookupService,
    private readonly snackBar: MatSnackBar,
    private readonly ref: MatDialogRef<TravelDocumentFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TravelDocumentFormDialogData
  ) {
    this.isEdit = !!data.row?.id;
  }

  ngOnInit(): void {
    const r = this.data.row;
    this.lookup.ensureCatalog('clients', 'bookings').subscribe({
      next: () => {
        this.form = this.fb.group({
          client: [this.lookup.findPick('client', r?.clientId)],
          booking: [this.lookup.findPick('booking', r?.bookingId)],
          docType: [r?.docType ?? 'PASSPORT', Validators.required],
          fileName: [r?.fileName ?? ''],
          storageRef: [r?.storageRef ?? ''],
        });
        this.ready = true;
      },
    });
  }

  get filterClientId(): number | null {
    const c = this.form?.get('client')?.value as TravelEntityPick | null;
    return c?.id ?? null;
  }

  onFilePicked(file: File | null): void {
    this.selectedFile = file;
    if (!file) {
      return;
    }
    this.form.patchValue({ fileName: file.name });
  }

  cancel(): void {
    this.ref.close(false);
  }

  save(): void {
    if (!this.form || this.form.invalid) {
      this.form?.markAllAsTouched();
      return;
    }
    if (!this.isEdit && !this.selectedFile && !this.form.get('storageRef')?.value) {
      this.snackBar.open('Please choose a file to upload.', 'Dismiss', { duration: 3500 });
      return;
    }
    const v = this.form.getRawValue();
    const client = v.client as TravelEntityPick | null;
    const booking = v.booking as TravelEntityPick | null;

    const persist$ = (storageRef?: string, fileName?: string) => {
      const payload = {
        clientId: client?.id,
        bookingId: booking?.id,
        docType: String(v.docType).trim(),
        fileName: fileName || v.fileName?.trim() || undefined,
        storageRef: storageRef || v.storageRef?.trim() || undefined,
      };
      return this.isEdit
        ? this.api.update({ ...payload, id: this.data.row!.id! })
        : this.api.add(payload);
    };

    this.saving = true;
    const req$ = this.selectedFile
      ? this.api.uploadFile(this.selectedFile).pipe(
          switchMap((uploaded) => persist$(uploaded.storageRef, uploaded.fileName))
        )
      : persist$();

    req$.pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => this.ref.close(true),
      error: (err: Error) =>
        this.snackBar.open(
          err?.message?.includes('25 MB')
            ? err.message
            : 'Save failed. File may exceed 1 MB server limit — restart travel service and gateway after update.',
          'Dismiss',
          { duration: 6000 }
        ),
    });
  }
}
