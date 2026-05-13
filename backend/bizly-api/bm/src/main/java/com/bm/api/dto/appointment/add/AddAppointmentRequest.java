package com.bm.api.dto.appointment.add;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.bm.api.enums.AppointmentStatus;
import com.bm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddAppointmentRequest {

	@NotNull(message = ApiDefaultValdiation.CUSTOMER_ID)
	private Long customerId;

	@NotNull(message = ApiDefaultValdiation.SERVICE_ID)
	private Long serviceId;

	@NotBlank(message = ApiDefaultValdiation.TITLE)
	private String title;

	@NotNull(message = ApiDefaultValdiation.START_TIME)
	private LocalDateTime startTime;

	private String notes;

	private AppointmentStatus status;
}
