import { Injectable } from '@angular/core';
import { Observable, from, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { parsePassportMrzFromText, PassportMrzParseResult } from '../utils/travel-passport-mrz.parser';
import { parseTravelDateOnly } from '../utils/travel-date.util';

export interface PassportOcrResult extends PassportMrzParseResult {
  rawText?: string;
}

@Injectable({ providedIn: 'root' })
export class TravelPassportOcrService {
  /**
   * OCR passport image and parse MRZ. PDFs are skipped (image-only).
   */
  scanFile(file: File): Observable<PassportOcrResult | null> {
    if (!file.type.startsWith('image/')) {
      return of(null);
    }
    return from(this.runOcr(file)).pipe(
      map((text) => {
        const parsed = parsePassportMrzFromText(text);
        if (!parsed) {
          return { rawText: text };
        }
        return { ...parsed, rawText: text };
      }),
      catchError(() => of(null))
    );
  }

  /** Apply OCR result to reactive form patch object (dates as Date). */
  toFormPatch(ocr: PassportOcrResult): Record<string, unknown> {
    const patch: Record<string, unknown> = {};
    if (ocr.fullName) {
      patch['fullName'] = ocr.fullName;
    }
    if (ocr.passportNo) {
      patch['passportNo'] = ocr.passportNo;
    }
    if (ocr.nationality) {
      patch['nationality'] = ocr.nationality;
      patch['passportPlaceOfIssue'] = patch['passportPlaceOfIssue'] ?? ocr.nationality;
    }
    if (ocr.passportType) {
      patch['passportType'] = ocr.passportType;
    }
    if (ocr.passportExpiryDate) {
      patch['passportExpiryDate'] = parseTravelDateOnly(ocr.passportExpiryDate);
    }
    return patch;
  }

  private async runOcr(file: File): Promise<string> {
    const { createWorker } = await import('tesseract.js');
    const worker = await createWorker('eng', 1, {
      logger: () => undefined,
    });
    try {
      const { data } = await worker.recognize(file);
      return data.text ?? '';
    } finally {
      await worker.terminate();
    }
  }
}
