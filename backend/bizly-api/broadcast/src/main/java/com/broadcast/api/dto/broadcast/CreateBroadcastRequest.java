package com.broadcast.api.dto.broadcast;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBroadcastRequest {

	@NotBlank
	@Size(max = 512)
	private String subject;

	@NotBlank
	private String body;

	@NotBlank
	private String targetType;

	private Long targetRoleId;

	private String customSegmentJson;

	/** When true, queues delivery immediately after save (same as calling send). */
	private boolean queueForSend;
}
