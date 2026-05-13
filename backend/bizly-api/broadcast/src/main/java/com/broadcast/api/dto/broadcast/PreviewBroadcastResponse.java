package com.broadcast.api.dto.broadcast;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreviewBroadcastResponse {

	private String subject;

	private String htmlBody;

	private String textBody;
}
