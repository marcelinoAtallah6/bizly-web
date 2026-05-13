import { PageResponse } from './api.types';

/** com.broadcast.api.enums.BroadcastTargetType */
export type BroadcastTargetType = 'ALL_USERS' | 'ALL_CUSTOMERS' | 'ROLE_BASED' | 'CUSTOM_SEGMENT';

/** com.broadcast.api.dto.broadcast.CreateBroadcastRequest */
export interface CreateBroadcastRequest {
  subject: string;
  body: string;
  targetType: string;
  targetRoleId?: number | null;
  customSegmentJson?: string | null;
  queueForSend?: boolean;
}

/** com.broadcast.api.dto.broadcast.CreateBroadcastResponse */
export interface CreateBroadcastResponse {
  id: number;
}

/** com.broadcast.api.dto.broadcast.PreviewBroadcastRequest */
export interface PreviewBroadcastRequest {
  subject?: string;
  body?: string;
  variables?: Record<string, string>;
}

/** com.broadcast.api.dto.broadcast.PreviewBroadcastResponse */
export interface PreviewBroadcastResponse {
  subject?: string;
  htmlBody?: string;
  textBody?: string;
}

/** com.broadcast.api.dto.broadcast.SendBroadcastRequest */
export interface SendBroadcastRequest {
  id: number;
}

/** com.broadcast.api.dto.broadcast.SendBroadcastResponse */
export interface SendBroadcastResponse {
  id: number;
  queued?: boolean;
}

/** com.broadcast.api.dto.broadcast.GetBroadcastRequest */
export interface GetBroadcastRequest {
  id: number;
}

/** com.broadcast.api.dto.broadcast.GetBroadcastResponse */
export interface GetBroadcastResponse {
  id: number;
  subject?: string;
  body?: string;
  targetType?: string;
  targetRoleId?: number | null;
  customSegmentJson?: string | null;
  status?: string;
  deliveryRequested?: boolean;
  createdAt?: string;
  sentAt?: string | null;
  createdBy?: string | null;
}

/** com.broadcast.api.dto.broadcast.GetsBroadcastsRequest */
export interface GetsBroadcastsRequest {
  pageNumber: number;
  pageSize: number;
}

/** com.broadcast.common.PageResponse<GetBroadcastResponse> */
export type GetsBroadcastsResponse = PageResponse<GetBroadcastResponse>;
