import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GlobalConstants } from 'src/app/common/GlobalConstants';
import {
  CreateBroadcastRequest,
  CreateBroadcastResponse,
  GetBroadcastRequest,
  GetBroadcastResponse,
  GetsBroadcastsRequest,
  GetsBroadcastsResponse,
  PreviewBroadcastRequest,
  PreviewBroadcastResponse,
  SendBroadcastRequest,
  SendBroadcastResponse,
} from 'src/app/core/models/broadcast.models';
import { BusinessApiService } from 'src/app/services/business-api.service';

@Injectable({
  providedIn: 'root',
})
export class BroadcastMessageService {
  constructor(private readonly api: BusinessApiService) {}

  create(body: CreateBroadcastRequest): Observable<CreateBroadcastResponse> {
    return this.api.postEnvelope<CreateBroadcastResponse>(
      GlobalConstants.API_ENDPOINTS.broadcast.message.create,
      body,
      'success-and-errors'
    );
  }

  preview(body: PreviewBroadcastRequest): Observable<PreviewBroadcastResponse> {
    return this.api.postEnvelope<PreviewBroadcastResponse>(
      GlobalConstants.API_ENDPOINTS.broadcast.message.preview,
      body,
      'errors'
    );
  }

  send(body: SendBroadcastRequest): Observable<SendBroadcastResponse> {
    return this.api.postEnvelope<SendBroadcastResponse>(
      GlobalConstants.API_ENDPOINTS.broadcast.message.send,
      body,
      'success-and-errors'
    );
  }

  get(body: GetBroadcastRequest): Observable<GetBroadcastResponse> {
    return this.api.postEnvelope<GetBroadcastResponse>(
      GlobalConstants.API_ENDPOINTS.broadcast.message.get,
      body,
      'errors'
    );
  }

  gets(body: GetsBroadcastsRequest): Observable<GetsBroadcastsResponse> {
    return this.api.postEnvelope<GetsBroadcastsResponse>(
      GlobalConstants.API_ENDPOINTS.broadcast.message.gets,
      body,
      'errors'
    );
  }
}

