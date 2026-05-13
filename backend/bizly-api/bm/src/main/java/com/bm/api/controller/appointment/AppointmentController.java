package com.bm.api.controller.appointment;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
import com.bm.api.service.appointment.IAppointmentService;
import com.bm.audit.Audited;
import com.bm.common.ApiMessages;
import com.bm.common.ApiResponse;
import com.bm.common.PageResponse;
import com.bm.exception.ServiceException;
import com.bm.security.MenuPermissionAction;
import com.bm.security.RequireMenuPermission;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

	private static final Logger log = LogManager.getLogger(AppointmentController.class);

	@Autowired
	private IAppointmentService appointmentService;

	@PostMapping("/add")
	@Audited(action = "BM_APPOINTMENT_ADD", resourceType = "APPOINTMENT")
	@RequireMenuPermission(menuRoute = "/bm/appointments", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddAppointmentResponse>> add(HttpServletRequest httpRequest,
			@RequestBody @Valid AddAppointmentRequest request) {

		log.info("[BM_APPOINTMENT][ADD] customerId={} serviceId={}", request.getCustomerId(), request.getServiceId());
		String user = httpRequest.getHeader("X-User");
		return ResponseEntity.ok(ApiResponse.success(appointmentService.add(request, user), ApiMessages.APPOINTMENT_ADDED));
	}

	@PostMapping("/update")
	@Audited(action = "BM_APPOINTMENT_UPDATE", resourceType = "APPOINTMENT")
	@RequireMenuPermission(menuRoute = "/bm/appointments", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateAppointmentResponse>> update(HttpServletRequest httpRequest,
			@RequestBody @Valid UpdateAppointmentRequest request) {

		log.info("[BM_APPOINTMENT][UPDATE] id={}", request.getId());
		String user = httpRequest.getHeader("X-User");
		return ResponseEntity
				.ok(ApiResponse.success(appointmentService.update(request, user), ApiMessages.APPOINTMENT_UPDATED));
	}

	@PostMapping("/cancel")
	@Audited(action = "BM_APPOINTMENT_CANCEL", resourceType = "APPOINTMENT")
	@RequireMenuPermission(menuRoute = "/bm/appointments", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<CancelAppointmentResponse>> cancel(HttpServletRequest httpRequest,
			@RequestBody @Valid CancelAppointmentRequest request) {

		log.info("[BM_APPOINTMENT][CANCEL] id={}", request.getId());
		String user = httpRequest.getHeader("X-User");
		return ResponseEntity
				.ok(ApiResponse.success(appointmentService.cancel(request, user), ApiMessages.APPOINTMENT_CANCELLED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/bm/appointments", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetAppointmentResponse>> get(
			@RequestBody @Valid GetAppointmentRequest request) {

		log.info("[BM_APPOINTMENT][GET] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(appointmentService.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/bm/appointments", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<CalendarAppointmentDto>>> gets(
			@RequestBody @Valid GetsAppointmentsRequest request) {

		log.info("[BM_APPOINTMENT][GETS] page={}", request.getPageNumber());
		return ResponseEntity.ok(ApiResponse.success(appointmentService.gets(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/range")
	@RequireMenuPermission(menuRoute = "/bm/appointments", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<List<CalendarAppointmentDto>>> range(
			@RequestBody @Valid AppointmentsByRangeRequest request) {

		log.info("[BM_APPOINTMENT][RANGE]");
		return ResponseEntity.ok(ApiResponse.success(appointmentService.listByRange(request), ApiMessages.SUCCESS));
	}

	@GetMapping("/calendar")
	@RequireMenuPermission(menuRoute = "/bm/appointments", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<List<CalendarAppointmentDto>>> calendar(
			@RequestParam(required = false) String month,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime rangeStart,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime rangeEnd) {

		log.info("[BM_APPOINTMENT][CALENDAR] month={} rangeStart={} rangeEnd={}", month, rangeStart, rangeEnd);

		if (month != null && !month.isBlank()) {
			try {
				YearMonth ym = YearMonth.parse(month.trim());
				return ResponseEntity.ok(
						ApiResponse.success(appointmentService.calendarForMonth(ym), ApiMessages.SUCCESS));
			} catch (DateTimeParseException ex) {
				throw new ServiceException("Invalid month format; use yyyy-MM", HttpStatus.BAD_REQUEST);
			}
		}
		if (rangeStart != null && rangeEnd != null) {
			return ResponseEntity.ok(ApiResponse.success(appointmentService.calendarForRange(rangeStart, rangeEnd),
					ApiMessages.SUCCESS));
		}

		throw new ServiceException("Provide either month (yyyy-MM) or both rangeStart and rangeEnd",
				HttpStatus.BAD_REQUEST);
	}
}
