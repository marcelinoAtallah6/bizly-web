import { PageResponse } from './api.types';

/** AppointmentStatus — mirrors backend enum */
export type AppointmentStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';

/** GET .../service-item/get response */
export interface GetServiceItemResponse {
  id: number;
  name: string;
  description?: string | null;
  price: number;
  durationMinutes: number;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string | null;
  updatedBy?: string | null;
}

export interface AddServiceItemRequest {
  name: string;
  description?: string | null;
  price: number;
  durationMinutes: number;
  active?: boolean | null;
}

export interface AddServiceItemResponse {
  id?: number;
}

export interface UpdateServiceItemRequest {
  id: number;
  name: string;
  description?: string | null;
  price: number;
  durationMinutes: number;
  active?: boolean | null;
}

export interface UpdateServiceItemResponse {
  success?: boolean;
}

export interface DeactivateServiceItemRequest {
  id: number;
}

export interface DeactivateServiceItemResponse {}

export interface GetServiceItemRequest {
  id: number;
}

export interface GetsServiceItemsRequest {
  pageNumber: number;
  pageSize: number;
  /** When true, include inactive rows */
  includeInactive?: boolean;
}

export type GetsServiceItemsResponse = PageResponse<GetServiceItemResponse>;

/** --- Appointments --- */

export interface AddAppointmentRequest {
  customerId: number;
  serviceId: number;
  title: string;
  /** ISO local datetime string accepted by Jackson LocalDateTime */
  startTime: string;
  notes?: string | null;
  status?: AppointmentStatus | null;
}

export interface AddAppointmentResponse {
  id?: number;
}

export interface UpdateAppointmentRequest {
  id: number;
  serviceId?: number | null;
  title?: string | null;
  startTime?: string | null;
  notes?: string | null;
  status?: AppointmentStatus | null;
}

export interface UpdateAppointmentResponse {
  success?: boolean;
}

export interface CancelAppointmentRequest {
  id: number;
}

export interface CancelAppointmentResponse {}

export interface GetAppointmentRequest {
  id: number;
}

export interface GetAppointmentResponse {
  id: number;
  customerId: number;
  customerName?: string | null;
  serviceId: number;
  service?: GetServiceItemResponse | null;
  title: string;
  startTime: string;
  endTime: string;
  status: AppointmentStatus;
  notes?: string | null;
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string | null;
  updatedBy?: string | null;
}

export interface GetsAppointmentsRequest {
  pageNumber: number;
  pageSize: number;
  customerId?: number | null;
  rangeStart?: string | null;
  rangeEnd?: string | null;
}

export interface CalendarAppointmentDto {
  id: number;
  title: string;
  startTime: string;
  endTime: string;
  status: AppointmentStatus;
  /** green | red | yellow | blue */
  color: string;
  serviceName?: string | null;
  customerName?: string | null;
}

export interface AppointmentsByRangeRequest {
  rangeStart: string;
  rangeEnd: string;
}
