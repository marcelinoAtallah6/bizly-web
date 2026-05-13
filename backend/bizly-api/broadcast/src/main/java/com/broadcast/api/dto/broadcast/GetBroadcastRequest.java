package com.broadcast.api.dto.broadcast;

import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetBroadcastRequest {

	@NotNull
	private Long id;
}
