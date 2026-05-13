package com.broadcast.api.service.broadcast;

import com.broadcast.api.dto.broadcast.CreateBroadcastRequest;
import com.broadcast.api.dto.broadcast.CreateBroadcastResponse;
import com.broadcast.api.dto.broadcast.GetBroadcastRequest;
import com.broadcast.api.dto.broadcast.GetBroadcastResponse;
import com.broadcast.api.dto.broadcast.GetsBroadcastsRequest;
import com.broadcast.api.dto.broadcast.PreviewBroadcastRequest;
import com.broadcast.api.dto.broadcast.PreviewBroadcastResponse;
import com.broadcast.api.dto.broadcast.SendBroadcastRequest;
import com.broadcast.api.dto.broadcast.SendBroadcastResponse;
import com.broadcast.common.PageResponse;

public interface IBroadcastService {

	CreateBroadcastResponse create(CreateBroadcastRequest request, String createdByUsername);

	PreviewBroadcastResponse preview(PreviewBroadcastRequest request);

	SendBroadcastResponse send(SendBroadcastRequest request);

	GetBroadcastResponse get(GetBroadcastRequest request);

	PageResponse<GetBroadcastResponse> gets(GetsBroadcastsRequest request);
}
