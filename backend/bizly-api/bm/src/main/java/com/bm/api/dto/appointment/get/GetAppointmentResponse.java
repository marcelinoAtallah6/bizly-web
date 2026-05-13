package com.bm.api.dto.appointment.get;

import java.time.LocalDateTime;

import com.bm.api.dto.serviceitem.get.GetServiceItemResponse;
import com.bm.api.enums.AppointmentStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAppointmentResponse {

	private Long id;
	private Long customerId;
	private String customerName;
	private Long serviceId;
	private GetServiceItemResponse service;
	private String title;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private AppointmentStatus status;
	private String notes;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;
}
