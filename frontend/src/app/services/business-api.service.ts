import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Observable, catchError, mergeMap, of, throwError } from 'rxjs';
import { ApiEnvelope } from '../core/models/api.types';

/** How API envelopes surface notifications (backend returns `{ success, message, data }`). */
export type EnvelopeNotify = 'silent' | 'errors' | 'success-and-errors';

@Injectable({
  providedIn: 'root',
})
export class BusinessApiService {
  constructor(
    private readonly http: HttpClient,
    private readonly snackBar: MatSnackBar
  ) {}

  /**
   * Generic business API call:
   * - Uses POST
   * - Sends JSON body
   * - Relies on interceptor for Authorization Bearer and X-DEVICE-ID
   */
  post<T>(url: string, body: unknown = {}): Observable<T> {
    return this.http.post<T>(url, body);
  }

  /**
   * POST where backend wraps payload in `{ success, message, data }`.
   * @param notify `errors` (default): snackbar on failure; `success-and-errors`: also show backend `message` on success; `silent`: no snackbars.
   */
  postEnvelope<T>(
    url: string,
    body: unknown = {},
    notify: EnvelopeNotify = 'errors'
  ): Observable<T> {
    return this.http.post<ApiEnvelope<T>>(url, body).pipe(
      mergeMap((res) => this.unwrapEnvelope(res, notify)),
      catchError((err: unknown) => this.handleHttpError(err, notify))
    );
  }

  /**
   * GET returning `{ success, message, data }` (e.g. BM calendar).
   */
  getEnvelope<T>(
    url: string,
    notify: EnvelopeNotify = 'errors'
  ): Observable<T> {
    return this.http.get<ApiEnvelope<T>>(url).pipe(
      mergeMap((res) => this.unwrapEnvelope(res, notify)),
      catchError((err: unknown) => this.handleHttpError(err, notify))
    );
  }

  /**
   * GET with query params (values omitted when null/undefined/'').
   */
  getEnvelopeParams<T>(
    url: string,
    params: Record<string, string | null | undefined>,
    notify: EnvelopeNotify = 'errors'
  ): Observable<T> {
    let hp = new HttpParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v != null && String(v).trim() !== '') {
        hp = hp.set(k, String(v));
      }
    });
    return this.http.get<ApiEnvelope<T>>(url, { params: hp }).pipe(
      mergeMap((res) => this.unwrapEnvelope(res, notify)),
      catchError((err: unknown) => this.handleHttpError(err, notify))
    );
  }

  private unwrapEnvelope<T>(
    res: ApiEnvelope<T>,
    notify: EnvelopeNotify
  ): Observable<T> {
    if (!res || res.success !== true) {
      const msg = res?.message ?? 'Request failed';
      if (notify !== 'silent') {
        this.snackBar.open(msg, 'Dismiss', { duration: 6000 });
      }
      return throwError(() => new Error(msg));
    }
    if (notify === 'success-and-errors' && res.message) {
      this.snackBar.open(res.message, 'Dismiss', { duration: 4000 });
    }
    return of(res.data as T);
  }

  private handleHttpError(err: unknown, notify: EnvelopeNotify): Observable<never> {
    if (err instanceof HttpErrorResponse) {
      const api = err.error as { message?: string } | null;
      const raw = api?.message ?? err.message;
      const msg =
        raw.includes('Http failure response') || raw.length > 240
          ? 'Request failed. Please try again.'
          : raw;
      if (notify !== 'silent') {
        this.snackBar.open(msg, 'Dismiss', { duration: 6000 });
      }
      return throwError(() => new Error(msg));
    }
    return throwError(() => err);
  }
}
