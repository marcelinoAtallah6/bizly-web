package com.bm.api.service.appointment;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import com.bm.api.dto.appointment.add.AddAppointmentRequest;
import com.bm.api.dto.appointment.add.AddAppointmentResponse;
import com.bm.api.dto.appointment.calendar.AppointmentsByRangeRequest;
import com.bm.api.dto.appointment.calendar.CalendarAppointmentDto;
import com.bm.api.dto.appointment.delete.CancelAppointmentRequest;
import com.bm.api.dto.appointment.delete.CancelAppointmentResponse;
import com.bm.api.dto.appointment.get.GetAppointmentRequest;
import com.bm.api.dto.appointment.get.GetAppointmentResponse;
import com.bm.api.dto.appointment.gets.GetsAppointmentsRequest;
import com.bm.api.dto.appointment.update.UpdateAppointmentRequest;
import com.bm.api.dto.appointment.update.UpdateAppointmentResponse;
import com.bm.common.PageResponse;

public interface IAppointmentService {

	AddAppointmentResponse add(AddAppointmentRequest request, String username);

	UpdateAppointmentResponse update(UpdateAppointmentRequest request, String username);

	CancelAppointmentResponse cancel(CancelAppointmentRequest request, String username);

	GetAppointmentResponse get(GetAppointmentRequest request);

	PageResponse<CalendarAppointmentDto> gets(GetsAppointmentsRequest request);

	List<CalendarAppointmentDto> listByRange(AppointmentsByRangeRequest request);

	List<CalendarAppointmentDto> calendarForMonth(YearMonth month);

	List<CalendarAppointmentDto> calendarForRange(LocalDateTime rangeStart, LocalDateTime rangeEnd);
}
