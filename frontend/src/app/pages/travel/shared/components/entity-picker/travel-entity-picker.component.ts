import { Component, Input, OnDestroy, OnInit, forwardRef } from '@angular/core';
import {
  ControlValueAccessor,
  FormControl,
  NG_VALUE_ACCESSOR,
} from '@angular/forms';
import { Subject, takeUntil } from 'rxjs';
import { TravelEntityPick, TravelEntityType } from '../../models/travel-entity-pick.model';
import { TravelLookupService } from '../../services/travel-lookup.service';

@Component({
  selector: 'app-travel-entity-picker',
  templateUrl: './travel-entity-picker.component.html',
  styleUrl: './travel-entity-picker.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TravelEntityPickerComponent),
      multi: true,
    },
  ],
})
export class TravelEntityPickerComponent implements ControlValueAccessor, OnInit, OnDestroy {
  @Input() entityType!: TravelEntityType;
  @Input() label = 'Select';
  @Input() hint = 'Type to search';
  @Input() required = false;
  /** When picking bookings, limit to this client. */
  @Input() filterBookingByClientId: number | null = null;

  readonly searchCtrl = new FormControl<string>('');
  filtered: TravelEntityPick[] = [];
  private all: TravelEntityPick[] = [];
  selected: TravelEntityPick | null = null;
  private readonly destroy$ = new Subject<void>();
  private onChange: (v: TravelEntityPick | null) => void = () => {};
  private onTouched: () => void = () => {};
  disabled = false;
  loading = true;

  constructor(private readonly lookup: TravelLookupService) {}

  ngOnInit(): void {
    const catalogKey =
      this.entityType === 'user'
        ? 'users'
        : (`${this.entityType}s` as 'clients' | 'packages' | 'bookings' | 'invoices');
    this.lookup.ensureCatalog(catalogKey).subscribe({
      next: () => {
        this.refreshOptions();
        this.loading = false;
      },
      error: () => (this.loading = false),
    });

    this.searchCtrl.valueChanges.pipe(takeUntil(this.destroy$)).subscribe((q) => {
      this.applyFilter(q ?? '');
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  writeValue(obj: TravelEntityPick | null): void {
    this.selected = obj;
    this.searchCtrl.setValue(obj ? this.displayPick(obj) : '', { emitEvent: false });
  }

  registerOnChange(fn: (v: TravelEntityPick | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    if (isDisabled) {
      this.searchCtrl.disable({ emitEvent: false });
    } else {
      this.searchCtrl.enable({ emitEvent: false });
    }
  }

  displayPick = (pick: TravelEntityPick | null): string => {
    if (!pick) {
      return '';
    }
    return pick.secondaryLabel ? `${pick.primaryLabel} — ${pick.secondaryLabel}` : pick.primaryLabel;
  };

  onOptionSelected(pick: TravelEntityPick): void {
    this.selected = pick;
    this.searchCtrl.setValue(this.displayPick(pick), { emitEvent: false });
    this.onChange(pick);
    this.onTouched();
  }

  onInputBlur(): void {
    this.onTouched();
    const text = (this.searchCtrl.value ?? '').trim();
    if (!text) {
      this.selected = null;
      this.onChange(null);
      return;
    }
    if (this.selected && this.displayPick(this.selected) === text) {
      return;
    }
    const match = this.all.find((p) => this.displayPick(p).toLowerCase() === text.toLowerCase());
    if (match) {
      this.onOptionSelected(match);
    }
  }

  clear(): void {
    this.selected = null;
    this.searchCtrl.setValue('');
    this.onChange(null);
    this.applyFilter('');
  }

  private refreshOptions(): void {
    this.all = this.lookup.picksFor(this.entityType, this.filterBookingByClientId);
    this.applyFilter(this.searchCtrl.value ?? '');
  }

  private applyFilter(query: string): void {
    const q = query.trim().toLowerCase();
    if (!q) {
      this.filtered = this.all.slice(0, 50);
      return;
    }
    this.filtered = this.all
      .filter(
        (p) =>
          p.primaryLabel.toLowerCase().includes(q) ||
          (p.secondaryLabel?.toLowerCase().includes(q) ?? false)
      )
      .slice(0, 50);
  }
}
