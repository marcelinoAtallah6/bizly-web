package com.bm.api.dto.appointment.delete;

import javax.validation.constraints.NotNull;

import com.bm.common.ApiDefaultValdiation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelAppointmentRequest {

	@NotNull(message = ApiDefaultValdiation.ID)
	private Long id;
}
