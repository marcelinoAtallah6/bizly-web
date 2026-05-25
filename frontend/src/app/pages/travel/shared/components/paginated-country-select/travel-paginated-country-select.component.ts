import { Component, forwardRef, Input, OnInit } from '@angular/core';
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatSelectChange } from '@angular/material/select';
import { finalize } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import { BusinessApiService } from 'src/app/services/business-api.service';

export interface CountryOption {
  id: number;
  iso2: string;
  name: string;
  label: string;
}

@Component({
  selector: 'app-travel-paginated-country-select',
  templateUrl: './travel-paginated-country-select.component.html',
  styleUrl: './travel-paginated-country-select.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => TravelPaginatedCountrySelectComponent),
      multi: true,
    },
  ],
})
export class TravelPaginatedCountrySelectComponent implements ControlValueAccessor, OnInit {
  @Input() label = 'Country';
  @Input() required = false;

  readonly searchCtrl = new FormControl('');
  countries: CountryOption[] = [];
  value: string | null = null;
  loading = false;
  hasMore = true;
  private page = 0;
  private readonly pageSize = 40;
  private onChange: (v: string | null) => void = () => {};
  private onTouched: () => void = () => {};

  constructor(private readonly api: BusinessApiService) {}

  ngOnInit(): void {
    this.loadPage(true);
    this.searchCtrl.valueChanges.subscribe(() => {
      this.loadPage(true);
    });
  }

  writeValue(v: string | null): void {
    this.value = v;
  }

  registerOnChange(fn: (v: string | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    if (isDisabled) {
      this.searchCtrl.disable({ emitEvent: false });
    } else {
      this.searchCtrl.enable({ emitEvent: false });
    }
  }

  onSelect(e: MatSelectChange): void {
    this.value = e.value as string;
    this.onChange(this.value);
    this.onTouched();
  }

  onOpened(open: boolean): void {
    if (open) {
      this.onTouched();
      setTimeout(() => this.bindPanelScroll(), 0);
    }
  }

  private bindPanelScroll(): void {
    const panel = document.querySelector('.cdk-overlay-pane .mat-mdc-select-panel') as HTMLElement | null;
    if (!panel || panel.dataset['travelCountryScroll'] === '1') {
      return;
    }
    panel.dataset['travelCountryScroll'] = '1';
    panel.addEventListener('scroll', () => {
      if (!this.hasMore || this.loading) {
        return;
      }
      if (panel.scrollHeight - panel.scrollTop - panel.clientHeight < 64) {
        this.loadPage(false);
      }
    });
  }

  private loadPage(reset: boolean): void {
    if (this.loading) {
      return;
    }
    if (reset) {
      this.page = 0;
      this.countries = [];
      this.hasMore = true;
    }
    if (!this.hasMore) {
      return;
    }
    this.loading = true;
    const q = (this.searchCtrl.value ?? '').toString().trim();
    this.api
      .postEnvelope<{ items?: CountryOption[]; totalPages?: number }>(
        GlobalConstants.API_ENDPOINTS.travel.lookup.countriesGets,
        { pageNumber: this.page, pageSize: this.pageSize, query: q || undefined },
        'silent'
      )
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (p) => {
          const items = p.items ?? [];
          this.countries = reset ? items : [...this.countries, ...items];
          this.hasMore = this.page + 1 < (p.totalPages ?? 0);
          this.page += 1;
        },
      });
  }
}
