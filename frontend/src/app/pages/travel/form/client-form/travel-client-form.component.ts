import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, debounceTime, filter, finalize, skip, switchMap, takeUntil } from 'rxjs';
import { TravelPassportOcrService } from '../../shared/services/travel-passport-ocr.service';
import { ToolbarButton } from 'src/app/pages/ui-components/button/toolbar/toolbar.component';
import { TravelClientService } from '../../services/travel-client.service';
import { TravelDocumentService } from '../../services/travel-document.service';
import { TravelDestinationService } from '../../services/travel-destination.service';
import {
  TRAVEL_LOYALTY_PROGRAMS,
  TRAVEL_LOYALTY_STATUSES,
  TRAVEL_PASSPORT_TYPES,
  TRAVEL_PHONE_COUNTRY_CODES,
  TRAVEL_PREFERRED_AIRLINES,
  TRAVEL_PREFERRED_HOTELS,
} from '../../shared/travel-client-form.constants';
import {
  travelOptionalEmailValidator,
  travelOptionalPassportValidator,
  travelPassportExpiryWarningValidator,
} from '../../shared/validators/travel-validators';
import { formatTravelDateOnly, parseTravelDateOnly } from '../../shared/utils/travel-date.util';
import { TravelPassportPreviewData } from '../../shared/components/passport-preview/travel-passport-preview.component';

@Component({
  selector: 'app-travel-client-form',
  templateUrl: './travel-client-form.component.html',
  styleUrls: ['./_travel-client-form.scss'],
})
export class TravelClientFormComponent implements OnInit, OnDestroy {
  form!: FormGroup;
  mode: 'create' | 'edit' = 'create';
  clientId: number | null = null;
  loading = false;
  saving = false;
  autoSaving = false;
  lastAutoSaved: Date | null = null;
  passportFile: File | null = null;
  passportPhotoUrl: string | null = null;
  scanPreviewOpen = false;
  scanProcessing = false;
  scanPreviewData: TravelPassportPreviewData | null = null;

  readonly passportTypes = [...TRAVEL_PASSPORT_TYPES];
  readonly phoneCodes = [...TRAVEL_PHONE_COUNTRY_CODES];
  readonly loyaltyPrograms = [...TRAVEL_LOYALTY_PROGRAMS];
  readonly loyaltyStatuses = [...TRAVEL_LOYALTY_STATUSES];
  readonly airlineOptions = [...TRAVEL_PREFERRED_AIRLINES];
  readonly hotelOptions = [...TRAVEL_PREFERRED_HOTELS];
  destinationOptions: string[] = [];

  private readonly destroy$ = new Subject<void>();
  private suppressAutoSave = false;

  get formToolbar(): ToolbarButton[] {
    return [{ id: 'back', icon: 'arrow_back', tooltip: 'Back', action: () => this.cancel() }];
  }

  get passportExpiryWarning(): boolean {
    return !!this.form?.get('passportExpiryDate')?.hasError('passportExpired');
  }

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly api: TravelClientService,
    private readonly documents: TravelDocumentService,
    private readonly destinations: TravelDestinationService,
    private readonly ocr: TravelPassportOcrService,
    private readonly snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.mode = (this.route.snapshot.data['mode'] as 'create' | 'edit') ?? 'create';
    const idParam = this.route.snapshot.paramMap.get('id');
    this.clientId = idParam ? Number(idParam) : null;

    this.form = this.fb.group({
      fullName: ['', [Validators.required, Validators.maxLength(200)]],
      email: ['', [travelOptionalEmailValidator()]],
      phoneCountryCode: ['+961'],
      phoneNumber: [''],
      passportNo: ['', [travelOptionalPassportValidator()]],
      nationality: [''],
      passportType: ['P', Validators.required],
      passportIssueDate: [null as Date | null],
      passportExpiryDate: [null as Date | null, [travelPassportExpiryWarningValidator()]],
      passportPlaceOfIssue: [''],
      nationalId: [''],
      passportScanRef: [''],
      notes: [''],
      active: [true],
      savedDestinations: [[] as string[]],
      preferredAirlines: [[] as string[]],
      preferredHotels: [[] as string[]],
      loyaltyProgram: ['None'],
      loyaltyPoints: [null as number | null, [Validators.min(0)]],
      loyaltyStatus: ['Active'],
    });

    this.loadDestinationOptions();

    if (this.mode === 'edit' && this.clientId != null) {
      this.loading = true;
      this.api
        .get({ id: this.clientId })
        .pipe(finalize(() => (this.loading = false)))
        .subscribe({ next: (c) => this.patchClient(c) });
    }

    this.form.valueChanges
      .pipe(
        skip(1),
        debounceTime(800),
        filter(() => !this.suppressAutoSave && this.mode === 'edit' && this.clientId != null && this.form.valid),
        takeUntil(this.destroy$)
      )
      .subscribe(() => this.autoSave());
  }

  ngOnDestroy(): void {
    this.revokePassportPhotoUrl();
    this.destroy$.next();
    this.destroy$.complete();
  }

  private revokePassportPhotoUrl(): void {
    if (this.passportPhotoUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(this.passportPhotoUrl);
    }
    this.passportPhotoUrl = null;
  }

  private loadDestinationOptions(): void {
    this.destinations.gets({ pageNumber: 0, pageSize: 200 }).subscribe({
      next: (p) => {
        const names = (p.items ?? [])
          .map((d) => d.name?.trim())
          .filter((n): n is string => !!n);
        this.destinationOptions = [...new Set(names)].sort();
      },
    });
  }

  private patchClient(c: {
    fullName?: string;
    email?: string;
    phone?: string;
    passportNo?: string;
    notes?: string;
    active?: boolean;
    travelProfile?: {
      nationality?: string;
      passportType?: string;
      passportIssueDate?: string;
      passportExpiryDate?: string;
      passportPlaceOfIssue?: string;
      nationalId?: string;
      passportScanStorageRef?: string;
      savedDestinations?: string[];
      preferredAirlines?: string[];
      preferredHotels?: string[];
      loyaltyProgram?: string;
      loyaltyPoints?: number;
      loyaltyStatus?: string;
    };
  }): void {
    this.suppressAutoSave = true;
    const p = c.travelProfile;
    const { code, number } = this.splitPhone(c.phone ?? '');
    this.form.patchValue({
      fullName: c.fullName ?? '',
      email: c.email ?? '',
      phoneCountryCode: code,
      phoneNumber: number,
      passportNo: c.passportNo ?? '',
      nationality: p?.nationality ?? '',
      passportType: p?.passportType ?? 'P',
      passportIssueDate: parseTravelDateOnly(p?.passportIssueDate),
      passportExpiryDate: parseTravelDateOnly(p?.passportExpiryDate),
      passportPlaceOfIssue: p?.passportPlaceOfIssue ?? '',
      nationalId: p?.nationalId ?? '',
      passportScanRef: p?.passportScanStorageRef ?? '',
      notes: c.notes ?? '',
      active: c.active !== false,
      savedDestinations: p?.savedDestinations ?? [],
      preferredAirlines: p?.preferredAirlines ?? [],
      preferredHotels: p?.preferredHotels ?? [],
      loyaltyProgram: p?.loyaltyProgram ?? 'None',
      loyaltyPoints: p?.loyaltyPoints ?? null,
      loyaltyStatus: p?.loyaltyStatus ?? 'Active',
    });
    this.refreshScanPreview();
    const scanRef = p?.passportScanStorageRef;
    if (scanRef) {
      this.loadPassportPhotoFromServer(scanRef);
    }
    setTimeout(() => (this.suppressAutoSave = false), 0);
  }

  private loadPassportPhotoFromServer(storageRef: string): void {
    if (!storageRef || storageRef.startsWith('http')) {
      return;
    }
    this.documents.viewBlobByRef(storageRef).subscribe({
      next: (blob) => {
        this.revokePassportPhotoUrl();
        if (blob.type.startsWith('image/')) {
          this.passportPhotoUrl = URL.createObjectURL(blob);
        }
      },
    });
  }

  private splitPhone(phone: string): { code: string; number: string } {
    const t = phone.trim();
    for (const c of TRAVEL_PHONE_COUNTRY_CODES) {
      if (t.startsWith(c.code)) {
        return { code: c.code, number: t.slice(c.code.length).trim() };
      }
    }
    return { code: '+961', number: t };
  }

  private buildPhone(): string | undefined {
    const code = (this.form.get('phoneCountryCode')?.value ?? '').toString().trim();
    const num = (this.form.get('phoneNumber')?.value ?? '').toString().trim();
    if (!num) {
      return undefined;
    }
    return `${code}${num}`.replace(/\s+/g, '');
  }

  onPassportFile(file: File | null): void {
    this.passportFile = file;
    this.revokePassportPhotoUrl();
    if (!file) {
      this.refreshScanPreview();
      return;
    }
    if (file.type.startsWith('image/')) {
      this.passportPhotoUrl = URL.createObjectURL(file);
      this.scanProcessing = true;
      this.ocr
        .scanFile(file)
        .pipe(finalize(() => (this.scanProcessing = false)))
        .subscribe((result) => {
          if (result && (result.passportNo || result.fullName)) {
            this.form.patchValue(this.ocr.toFormPatch(result));
            this.snackBar.open('Passport scanned — fields auto-filled. Please verify.', 'OK', {
              duration: 5000,
            });
          } else if (result?.rawText) {
            this.snackBar.open(
              'Could not read MRZ clearly — enter details manually.',
              'OK',
              { duration: 4000 }
            );
          }
          this.refreshScanPreview();
        });
    } else {
      this.snackBar.open('PDF uploaded. OCR works on JPG/PNG — verify fields manually.', 'OK', {
        duration: 4000,
      });
      this.refreshScanPreview();
    }
  }

  refreshScanPreview(): void {
    const v = this.form.getRawValue();
    if (!v.passportScanRef && !this.passportFile) {
      this.scanPreviewData = null;
      return;
    }
    this.scanPreviewData = {
      fullName: v.fullName,
      nationality: v.nationality,
      passportNo: v.passportNo,
      passportIssueDate: formatTravelDateOnly(v.passportIssueDate),
      passportExpiryDate: formatTravelDateOnly(v.passportExpiryDate),
      passportPlaceOfIssue: v.passportPlaceOfIssue,
      passportType: v.passportType,
      nationalId: v.nationalId,
      countryCode: v.nationality,
    };
  }

  cancel(): void {
    this.router.navigate(['/travel/clients']);
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.persist(false);
  }

  private autoSave(): void {
    this.persist(true);
  }

  private persist(silent: boolean): void {
    const buildPayload = (passportScanStorageRef?: string) => {
      const v = this.form.getRawValue();
      return {
        fullName: String(v.fullName).trim(),
        email: v.email?.trim() || undefined,
        phone: this.buildPhone(),
        passportNo: v.passportNo?.trim() || undefined,
        notes: v.notes?.trim() || undefined,
        active: !!v.active,
        travelProfile: {
          nationality: v.nationality?.trim() || undefined,
          passportType: v.passportType || undefined,
          passportIssueDate: formatTravelDateOnly(v.passportIssueDate),
          passportExpiryDate: formatTravelDateOnly(v.passportExpiryDate),
          passportPlaceOfIssue: v.passportPlaceOfIssue?.trim() || undefined,
          nationalId: v.nationalId?.trim() || undefined,
          passportScanStorageRef:
            passportScanStorageRef || v.passportScanRef?.trim() || undefined,
          savedDestinations: (v.savedDestinations as string[]) ?? [],
          preferredAirlines: (v.preferredAirlines as string[]) ?? [],
          preferredHotels: (v.preferredHotels as string[]) ?? [],
          loyaltyProgram: v.loyaltyProgram === 'None' ? undefined : v.loyaltyProgram,
          loyaltyPoints: v.loyaltyPoints != null ? Number(v.loyaltyPoints) : undefined,
          loyaltyStatus: v.loyaltyStatus || undefined,
        },
      };
    };

    const save$ = (passportScanStorageRef?: string) => {
      const payload = buildPayload(passportScanStorageRef);
      return this.mode === 'edit' && this.clientId != null
        ? this.api.update({ ...payload, id: this.clientId })
        : this.api.add(payload);
    };

    const runSave = (passportScanStorageRef?: string) => {
      if (silent) {
        this.autoSaving = true;
      } else {
        this.saving = true;
      }
      save$(passportScanStorageRef)
        .pipe(
          finalize(() => {
            this.saving = false;
            this.autoSaving = false;
          })
        )
        .subscribe({
          next: (res) => {
            if (!silent) {
              this.snackBar.open(
                this.mode === 'edit' ? 'Client updated.' : 'Client created.',
                'Dismiss',
                { duration: 3000 }
              );
              this.router.navigate(['/travel/clients']);
              return;
            }
            this.lastAutoSaved = new Date();
            if (this.mode === 'create' && res && typeof res === 'object' && 'id' in res) {
              this.clientId = (res as { id: number }).id;
              this.mode = 'edit';
            }
          },
          error: () => {
            if (!silent) {
              this.snackBar.open('Save failed. Check passport upload and try again.', 'Dismiss', {
                duration: 4000,
              });
            }
          },
        });
    };

    if (this.passportFile) {
      const file = this.passportFile;
      this.passportFile = null;
      if (silent) {
        this.autoSaving = true;
      } else {
        this.saving = true;
      }
      this.documents
        .uploadFile(file)
        .pipe(
          switchMap((u) => {
            this.form.patchValue({ passportScanRef: u.storageRef });
            return save$(u.storageRef);
          }),
          finalize(() => {
            this.saving = false;
            this.autoSaving = false;
          })
        )
        .subscribe({
          next: () => {
            this.loadPassportPhotoFromServer(this.form.get('passportScanRef')?.value);
            this.refreshScanPreview();
            if (!silent) {
              this.snackBar.open('Client saved.', 'Dismiss', { duration: 3000 });
              this.router.navigate(['/travel/clients']);
            } else {
              this.lastAutoSaved = new Date();
            }
          },
          error: () => {
            this.passportFile = file;
            if (!silent) {
              this.snackBar.open('Passport scan upload failed.', 'Dismiss', { duration: 4000 });
            }
          },
        });
    } else {
      runSave();
    }
  }
}
