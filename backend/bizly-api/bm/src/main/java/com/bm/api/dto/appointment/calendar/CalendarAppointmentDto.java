package com.bm.api.dto.appointment.calendar;

import java.time.LocalDateTime;

import com.bm.api.enums.AppointmentStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CalendarAppointmentDto {

	private Long id;
	private String title;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private AppointmentStatus status;
	private String color;
	private String serviceName;
	private String customerName;
}
