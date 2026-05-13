import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  AddAppointmentRequest,
  AddAppointmentResponse,
  AppointmentsByRangeRequest,
  CalendarAppointmentDto,
  CancelAppointmentRequest,
  CancelAppointmentResponse,
  GetAppointmentRequest,
  GetAppointmentResponse,
  GetsAppointmentsRequest,
  UpdateAppointmentRequest,
  UpdateAppointmentResponse,
} from 'src/app/core/models/bm.models';
import { PageResponse as PageResponseType } from 'src/app/core/models/api.types';
import { BusinessApiService } from 'src/app/services/business-api.service';

/** Page envelope for appointments GETS returns CalendarAppointmentDto rows */
export type GetsAppointmentsPageResponse = PageResponseType<CalendarAppointmentDto>;

@Injectable({
  providedIn: 'root',
})
export class BmAppointmentService {
  constructor(private readonly api: BusinessApiService) {}

  gets(body: GetsAppointmentsRequest): Observable<GetsAppointmentsPageResponse> {
    return this.api.postEnvelope<GetsAppointmentsPageResponse>(
      GlobalConstants.API_ENDPOINTS.bm.appointment.gets,
      body
    );
  }

  get(body: GetAppointmentRequest): Observable<GetAppointmentResponse> {
    return this.api.postEnvelope<GetAppointmentResponse>(
      GlobalConstants.API_ENDPOINTS.bm.appointment.get,
      body
    );
  }

  add(body: AddAppointmentRequest): Observable<AddAppointmentResponse> {
    return this.api.postEnvelope<AddAppointmentResponse>(
      GlobalConstants.API_ENDPOINTS.bm.appointment.add,
      body,
      'success-and-errors'
    );
  }

  update(body: UpdateAppointmentRequest): Observable<UpdateAppointmentResponse> {
    return this.api.postEnvelope<UpdateAppointmentResponse>(
      GlobalConstants.API_ENDPOINTS.bm.appointment.update,
      body,
      'success-and-errors'
    );
  }

  cancel(body: CancelAppointmentRequest): Observable<CancelAppointmentResponse> {
    return this.api.postEnvelope<CancelAppointmentResponse>(
      GlobalConstants.API_ENDPOINTS.bm.appointment.cancel,
      body,
      'success-and-errors'
    );
  }

  range(body: AppointmentsByRangeRequest): Observable<CalendarAppointmentDto[]> {
    return this.api.postEnvelope<CalendarAppointmentDto[]>(
      GlobalConstants.API_ENDPOINTS.bm.appointment.range,
      body
    );
  }

  /** GET calendar — month `yyyy-MM` or rangeStart + rangeEnd ISO datetimes */
  calendar(params: {
    month?: string | null;
    rangeStart?: string | null;
    rangeEnd?: string | null;
  }): Observable<CalendarAppointmentDto[]> {
    return this.api.getEnvelopeParams<CalendarAppointmentDto[]>(
      GlobalConstants.API_ENDPOINTS.bm.appointment.calendar,
      {
        month: params.month ?? undefined,
        rangeStart: params.rangeStart ?? undefined,
        rangeEnd: params.rangeEnd ?? undefined,
      }
    );
  }
}
