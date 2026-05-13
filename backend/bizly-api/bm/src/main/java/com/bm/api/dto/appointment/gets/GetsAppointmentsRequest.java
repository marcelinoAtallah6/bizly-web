package com.bm.api.dto.appointment.gets;

import java.time.LocalDateTime;

import com.bm.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetsAppointmentsRequest extends PageRequest {

	private Long customerId;

	private LocalDateTime rangeStart;

	private LocalDateTime rangeEnd;
}
