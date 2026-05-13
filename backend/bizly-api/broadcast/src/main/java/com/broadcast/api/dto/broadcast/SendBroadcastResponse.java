package com.broadcast.api.dto.broadcast;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendBroadcastResponse {

	private Long id;
	private boolean queued;
}
