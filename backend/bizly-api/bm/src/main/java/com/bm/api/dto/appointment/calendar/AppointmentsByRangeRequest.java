package com.bm.api.dto.appointment.calendar;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import com.bm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentsByRangeRequest {

	@NotNull(message = ApiDefaultValdiation.RANGE_START)
	private LocalDateTime rangeStart;

	@NotNull(message = ApiDefaultValdiation.RANGE_END)
	private LocalDateTime rangeEnd;
}
