import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class BusinessApiService {
  constructor(private readonly http: HttpClient) {}

  /**
   * Generic business API call:
   * - Uses POST
   * - Sends JSON body
   * - Relies on interceptor for Authorization Bearer and X-DEVICE-ID
   */
  post<T>(url: string, body: unknown = {}): Observable<T> {
    return this.http.post<T>(url, body);
  }
}
