package com.broadcast.api.dto.broadcast;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetBroadcastResponse {

	private Long id;
	private String subject;
	private String body;
	private String targetType;
	private Long targetRoleId;
	private String customSegmentJson;
	private String status;
	private boolean deliveryRequested;
	private LocalDateTime createdAt;
	private LocalDateTime sentAt;
	private String createdBy;
}
