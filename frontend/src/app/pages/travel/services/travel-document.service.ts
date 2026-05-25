import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddTravelDocumentRequest,
  GetsTravelDocumentsResponse,
  TravelPageRequest,
  UpdateTravelDocumentRequest,
  UploadTravelFileResponse,
} from 'src/app/core/models/travel.models';
import { ApiEnvelope } from 'src/app/core/models/api.types';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({ providedIn: 'root' })
export class TravelDocumentService {
  constructor(
    private readonly api: BusinessApiService,
    private readonly http: HttpClient
  ) {}

  gets(body: TravelPageRequest): Observable<GetsTravelDocumentsResponse> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.document.gets, body);
  }

  add(body: AddTravelDocumentRequest): Observable<{ id: number }> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.document.add, body, 'success-and-errors');
  }

  update(body: UpdateTravelDocumentRequest): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.document.update, body, 'success-and-errors');
  }

  delete(body: { id: number }): Observable<unknown> {
    return this.api.postEnvelope(GlobalConstants.API_ENDPOINTS.travel.document.delete, body, 'success-and-errors');
  }

  downloadBlob(body: { id: number }): Observable<Blob> {
    return this.http.post(GlobalConstants.API_ENDPOINTS.travel.document.download, body, {
      responseType: 'blob',
    });
  }

  viewBlob(body: { id: number }): Observable<Blob> {
    return this.http.post(GlobalConstants.API_ENDPOINTS.travel.document.view, body, {
      responseType: 'blob',
    });
  }

  viewBlobByRef(storageRef: string): Observable<Blob> {
    return this.http.post(
      GlobalConstants.API_ENDPOINTS.travel.document.viewRef,
      { storageRef },
      { responseType: 'blob' }
    );
  }

  downloadBlobByRef(storageRef: string): Observable<Blob> {
    return this.http.post(
      GlobalConstants.API_ENDPOINTS.travel.document.downloadRef,
      { storageRef },
      { responseType: 'blob' }
    );
  }

  uploadFile(file: File): Observable<UploadTravelFileResponse> {
    const maxBytes = 25 * 1024 * 1024;
    if (file.size > maxBytes) {
      throw new Error('File exceeds 25 MB limit.');
    }
    const form = new FormData();
    form.append('file', file, file.name);
    return this.http
      .post<ApiEnvelope<UploadTravelFileResponse>>(GlobalConstants.API_ENDPOINTS.travel.document.upload, form)
      .pipe(map((env) => env.data as UploadTravelFileResponse));
  }
}
