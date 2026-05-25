import { Component, Input } from '@angular/core';
import { travelPassportAccent, travelPassportMrzLine } from '../../utils/travel-passport-theme.util';

export interface TravelPassportPreviewData {
  fullName?: string;
  nationality?: string;
  passportNo?: string;
  passportIssueDate?: string;
  passportExpiryDate?: string;
  passportPlaceOfIssue?: string;
  passportType?: string;
  nationalId?: string;
  countryCode?: string;
}

@Component({
  selector: 'app-travel-passport-preview',
  templateUrl: './travel-passport-preview.component.html',
  styleUrl: './travel-passport-preview.component.scss',
})
export class TravelPassportPreviewComponent {
  @Input() title = 'TRAVEL DOCUMENT';
  @Input() subtitle = 'From traveler profile';
  /** Blob or remote URL of uploaded passport scan (replaces placeholder icon). */
  @Input() photoUrl: string | null = null;
  @Input() set data(value: TravelPassportPreviewData | null) {
    this._data = value ?? {};
    this.refreshStyles();
  }
  get data(): TravelPassportPreviewData {
    return this._data;
  }

  private _data: TravelPassportPreviewData = {};
  accentVars: Record<string, string> = {};
  mrz = '';

  private refreshStyles(): void {
    const accent = travelPassportAccent(this._data.countryCode ?? this._data.nationality);
    this.accentVars = {
      '--passport-primary': accent.primary,
      '--passport-secondary': accent.secondary,
      '--passport-paper': accent.paper,
    };
    this.mrz = travelPassportMrzLine({
      fullName: this._data.fullName,
      passportNo: this._data.passportNo,
      nationality: this._data.nationality,
      passportType: this._data.passportType,
    });
  }

  splitName(): { surname: string; given: string } {
    const raw = (this._data.fullName ?? '—').trim();
    const parts = raw.split(/\s+/);
    if (parts.length < 2) {
      return { surname: raw.toUpperCase(), given: '—' };
    }
    return {
      surname: parts[parts.length - 1].toUpperCase(),
      given: parts.slice(0, -1).join(' ').toUpperCase(),
    };
  }
}
