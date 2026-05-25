import { Component, Inject, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import {
  TravelItineraryDay,
  TravelPackageAddon,
  TravelPackageDestination,
  TravelPackageMedia,
  TravelPackageRow,
} from 'src/app/core/models/travel.models';
import { TravelDestinationRow } from 'src/app/core/models/travel.models';
import { TravelDestinationService } from '../../services/travel-destination.service';
import { TravelPackageService } from '../../services/travel-package.service';

export interface TravelPackageFormDialogData {
  row?: TravelPackageRow;
}

@Component({
  selector: 'app-travel-package-form-dialog',
  templateUrl: './travel-package-form-dialog.component.html',
  styleUrl: './travel-package-form-dialog.component.scss',
})
export class TravelPackageFormDialogComponent implements OnInit {
  form!: FormGroup;
  saving = false;
  loading = false;
  readonly isEdit: boolean;
  destinations: TravelDestinationRow[] = [];

  constructor(
    private readonly fb: FormBuilder,
    private readonly api: TravelPackageService,
    private readonly destinationApi: TravelDestinationService,
    private readonly ref: MatDialogRef<TravelPackageFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TravelPackageFormDialogData
  ) {
    this.isEdit = !!data.row?.id;
  }

  get itineraryDays(): FormArray {
    return this.form.get('itineraryDays') as FormArray;
  }

  get mediaItems(): FormArray {
    return this.form.get('mediaItems') as FormArray;
  }

  get packageDestinations(): FormArray {
    return this.form.get('packageDestinations') as FormArray;
  }

  get packageAddons(): FormArray {
    return this.form.get('packageAddons') as FormArray;
  }

  ngOnInit(): void {
    this.destinationApi.gets({ pageNumber: 0, pageSize: 500 }).subscribe({
      next: (p) => (this.destinations = (p.items ?? []).filter((d) => d.active !== false)),
    });

    this.form = this.fb.group({
      code: [''],
      name: ['', Validators.required],
      description: [''],
      destination: [''],
      durationDays: [null],
      basePrice: [0, [Validators.required, Validators.min(0)]],
      currency: ['USD'],
      active: [true],
      inclusions: [''],
      exclusions: [''],
      maxCapacityPerDay: [null, Validators.min(1)],
      itineraryDays: this.fb.array([]),
      packageDestinations: this.fb.array([]),
      packageAddons: this.fb.array([]),
      mediaItems: this.fb.array([]),
    });

    if (this.isEdit && this.data.row?.id) {
      this.loading = true;
      this.api
        .get({ id: this.data.row.id })
        .pipe(finalize(() => (this.loading = false)))
        .subscribe({ next: (p) => this.patchForm(p) });
    } else if (this.data.row) {
      this.patchForm(this.data.row);
    } else {
      this.addItineraryDay();
    }
  }

  addPackageDestination(dest?: TravelPackageDestination): void {
    this.packageDestinations.push(
      this.fb.group({
        destinationId: [dest?.destinationId ?? null],
        destinationName: [dest?.destinationName ?? '', Validators.required],
        nights: [dest?.nights ?? 1, Validators.min(1)],
      })
    );
  }

  onDestinationSelected(index: number, destId: number | null): void {
    const grp = this.packageDestinations.at(index) as FormGroup;
    const dest = this.destinations.find((d) => d.id === destId);
    grp.patchValue({
      destinationId: destId,
      destinationName: dest?.name ?? grp.get('destinationName')?.value ?? '',
    });
  }

  primaryDestinationLabel(id: number | null): string {
    const dest = this.destinations.find((d) => d.id === id);
    return dest ? `${dest.name}${dest.country ? ', ' + dest.country : ''}` : '';
  }

  removePackageDestination(index: number): void {
    this.packageDestinations.removeAt(index);
  }

  addPackageAddon(addon?: TravelPackageAddon): void {
    this.packageAddons.push(
      this.fb.group({
        code: [addon?.code ?? ''],
        name: [addon?.name ?? '', Validators.required],
        description: [addon?.description ?? ''],
        price: [addon?.price ?? 0, Validators.min(0)],
      })
    );
  }

  removePackageAddon(index: number): void {
    this.packageAddons.removeAt(index);
  }

  addItineraryDay(day?: TravelItineraryDay): void {
    const n = this.itineraryDays.length + 1;
    this.itineraryDays.push(
      this.fb.group({
        dayNumber: [day?.dayNumber ?? n, Validators.required],
        title: [day?.title ?? `Day ${n}`, Validators.required],
        details: [day?.details ?? ''],
      })
    );
  }

  removeItineraryDay(index: number): void {
    this.itineraryDays.removeAt(index);
  }

  onMediaFilePick(index: number, file: File | null): void {
    const grp = this.mediaItems.at(index) as FormGroup;
    if (!file) {
      grp.patchValue({ fileName: '', storageRef: '' });
      return;
    }
    grp.patchValue({
      fileName: file.name,
      storageRef: `uploads/packages/${Date.now()}/${file.name}`,
    });
  }

  addMediaItem(item?: TravelPackageMedia): void {
    this.mediaItems.push(
      this.fb.group({
        fileName: [item?.fileName ?? ''],
        storageRef: [item?.storageRef ?? ''],
      })
    );
  }

  removeMediaItem(index: number): void {
    this.mediaItems.removeAt(index);
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
    const itinerary: TravelItineraryDay[] = (v.itineraryDays ?? []).map((d: TravelItineraryDay) => ({
      dayNumber: Number(d.dayNumber),
      title: String(d.title).trim(),
      details: d.details?.trim() || undefined,
    }));
    const media: TravelPackageMedia[] = (v.mediaItems ?? [])
      .filter((m: TravelPackageMedia) => m.fileName?.trim())
      .map((m: TravelPackageMedia) => ({
        fileName: m.fileName.trim(),
        storageRef: m.storageRef?.trim() || undefined,
      }));

    const destinations: TravelPackageDestination[] = (v.packageDestinations ?? []).map(
      (d: TravelPackageDestination) => ({
        destinationId: d.destinationId != null ? Number(d.destinationId) : undefined,
        destinationName: String(d.destinationName).trim(),
        nights: d.nights != null ? Number(d.nights) : undefined,
      })
    );
    const addons: TravelPackageAddon[] = (v.packageAddons ?? [])
      .filter((a: TravelPackageAddon) => a.name?.trim())
      .map((a: TravelPackageAddon) => ({
        code: a.code?.trim() || undefined,
        name: String(a.name).trim(),
        description: a.description?.trim() || undefined,
        price: a.price != null ? Number(a.price) : undefined,
      }));

    const payload = {
      code: v.code?.trim() || undefined,
      name: String(v.name).trim(),
      description: v.description?.trim() || undefined,
      destination: v.destination?.trim() || undefined,
      durationDays: v.durationDays != null ? Number(v.durationDays) : undefined,
      basePrice: Number(v.basePrice),
      currency: v.currency?.trim() || 'USD',
      active: !!v.active,
      maxCapacityPerDay: v.maxCapacityPerDay != null ? Number(v.maxCapacityPerDay) : undefined,
      inclusions: v.inclusions?.trim() || undefined,
      exclusions: v.exclusions?.trim() || undefined,
      destinations: destinations.length ? destinations : undefined,
      addons: addons.length ? addons : undefined,
      itinerary: itinerary.length ? itinerary : undefined,
      media: media.length ? media : undefined,
    };
    this.saving = true;
    const req$ = this.isEdit
      ? this.api.update({ ...payload, id: this.data.row!.id! })
      : this.api.add(payload);
    req$.pipe(finalize(() => (this.saving = false))).subscribe({
      next: () => this.ref.close(true),
    });
  }

  private patchForm(p: TravelPackageRow): void {
    this.form.patchValue({
      code: p.code ?? '',
      name: p.name ?? '',
      description: p.description ?? '',
      destination: p.destination ?? '',
      durationDays: p.durationDays ?? null,
      basePrice: p.basePrice ?? 0,
      currency: p.currency ?? 'USD',
      active: p.active !== false,
      maxCapacityPerDay: p.maxCapacityPerDay ?? null,
      inclusions: p.inclusions ?? '',
      exclusions: p.exclusions ?? '',
    });
    for (const d of p.destinations ?? []) {
      this.addPackageDestination(d);
    }
    for (const a of p.addons ?? []) {
      this.addPackageAddon(a);
    }
    for (const d of p.itinerary ?? []) {
      this.addItineraryDay(d);
    }
    if (!p.itinerary?.length) {
      this.addItineraryDay();
    }
    for (const m of p.media ?? []) {
      this.addMediaItem(m);
    }
  }
}
