package com.bm.api.dto.appointment.update;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import com.bm.api.enums.AppointmentStatus;
import com.bm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAppointmentRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;

	private Long serviceId;

	private String title;

	private LocalDateTime startTime;

	private String notes;

	private AppointmentStatus status;
}
