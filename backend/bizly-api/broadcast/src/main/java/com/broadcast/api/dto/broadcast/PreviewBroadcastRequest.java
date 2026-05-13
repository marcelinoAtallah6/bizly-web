package com.broadcast.api.dto.broadcast;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreviewBroadcastRequest {

	private String subject;

	private String body;

	private Map<String, String> variables;
}
