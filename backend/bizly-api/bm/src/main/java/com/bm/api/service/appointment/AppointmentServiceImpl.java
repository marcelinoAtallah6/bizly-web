package com.bm.api.service.appointment;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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
import com.bm.api.repository.mapper.ServiceItemDtoMapper;
import com.bm.api.enums.AppointmentStatus;
import com.bm.api.model.Appointment;
import com.bm.api.model.ServiceItem;
import com.bm.api.model.customer.KycCustomerRef;
import com.bm.api.repository.AppointmentRepository;
import com.bm.api.repository.KycCustomerRefRepository;
import com.bm.api.repository.ServiceItemRepository;
import com.bm.api.service.notif.INotifInboxService;
import com.bm.api.service.notif.NotifCategory;
import com.bm.api.service.notif.NotifPublishRequest;
import com.bm.api.service.notif.NotifSeverity;
import com.bm.common.ApiMessages;
import com.bm.common.PageResponse;
import com.bm.exception.ServiceException;
import com.bm.security.BusinessContextHolder;

@Service
public class AppointmentServiceImpl implements IAppointmentService {

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private ServiceItemRepository serviceItemRepository;

	@Autowired
	private KycCustomerRefRepository kycCustomerRefRepository;

	@Autowired
	private INotifInboxService notifInbox;

	private static final String APPT_LINK = "/bm/appointments";
	private static final String APPT_RESOURCE = "APPOINTMENT";

	@Override
	public AddAppointmentResponse add(AddAppointmentRequest request, String username) {

		Long businessId = BusinessContextHolder.requireBusinessId();

		assertCustomerExists(request.getCustomerId());

		ServiceItem service = requireBookableService(request.getServiceId());

		LocalDateTime start = request.getStartTime();
		LocalDateTime end = start.plusMinutes(service.getDurationMinutes());

		AppointmentStatus status = request.getStatus() != null ? request.getStatus() : AppointmentStatus.PENDING;

		assertNoOverlapIfScheduling(request.getCustomerId(), start, end, null, status);

		Appointment a = new Appointment();
		a.setBusinessId(businessId);
		a.setCustomerId(request.getCustomerId());
		a.setServiceId(service.getId());
		a.setTitle(request.getTitle());
		a.setStartTime(start);
		a.setEndTime(end);
		a.setStatus(status);
		a.setNotes(request.getNotes());
		LocalDateTime now = LocalDateTime.now();
		a.setCreatedAt(now);
		a.setUpdatedAt(now);
		a.setCreatedBy(username);
		a.setUpdatedBy(username);

		appointmentRepository.save(a);

		publishAppointmentNotice(a, username, NotifSeverity.SUCCESS, "Appointment booked",
				buildBody("Booked for ", a));

		AddAppointmentResponse res = new AddAppointmentResponse();
		res.setId(a.getId());
		return res;
	}

	@Override
	public UpdateAppointmentResponse update(UpdateAppointmentRequest request, String username) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		Appointment a = appointmentRepository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.APPOINTMENT_NOT_FOUND, HttpStatus.NOT_FOUND));

		Long resolvedServiceId = request.getServiceId() != null ? request.getServiceId() : a.getServiceId();
		if (resolvedServiceId == null) {
			throw new ServiceException(ApiMessages.SERVICE_ITEM_NOT_FOUND, HttpStatus.BAD_REQUEST);
		}
		ServiceItem service = serviceItemRepository.findById(resolvedServiceId)
				.orElseThrow(() -> new ServiceException(ApiMessages.SERVICE_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND));

		if (request.getServiceId() != null && !request.getServiceId().equals(a.getServiceId())) {
			requireBookableService(service.getId());
		}

		LocalDateTime start = request.getStartTime() != null ? request.getStartTime() : a.getStartTime();
		LocalDateTime end = start.plusMinutes(service.getDurationMinutes());

		if (request.getTitle() != null) {
			a.setTitle(request.getTitle());
		}
		if (request.getNotes() != null) {
			a.setNotes(request.getNotes());
		}

		AppointmentStatus newStatus = request.getStatus() != null ? request.getStatus() : a.getStatus();

		a.setServiceId(service.getId());
		a.setStartTime(start);
		a.setEndTime(end);
		a.setStatus(newStatus);
		a.setUpdatedAt(LocalDateTime.now());
		a.setUpdatedBy(username);

		assertNoOverlapIfScheduling(a.getCustomerId(), start, end, a.getId(), newStatus);

		AppointmentStatus previousStatus = a.getStatus();
		appointmentRepository.save(a);

		if (previousStatus != newStatus && newStatus != null) {
			NotifSeverity sev = newStatus == AppointmentStatus.COMPLETED ? NotifSeverity.SUCCESS
					: newStatus == AppointmentStatus.CANCELLED ? NotifSeverity.WARN
					: NotifSeverity.INFO;
			String title = "Appointment " + newStatus.name().toLowerCase();
			publishAppointmentNotice(a, username, sev, title, buildBody("Scheduled for ", a));
		} else {
			publishAppointmentNotice(a, username, NotifSeverity.INFO, "Appointment updated",
					buildBody("Scheduled for ", a));
		}

		return new UpdateAppointmentResponse();
	}

	@Override
	public CancelAppointmentResponse cancel(CancelAppointmentRequest request, String username) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		Appointment a = appointmentRepository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.APPOINTMENT_NOT_FOUND, HttpStatus.NOT_FOUND));

		a.setStatus(AppointmentStatus.CANCELLED);
		a.setUpdatedAt(LocalDateTime.now());
		a.setUpdatedBy(username);
		appointmentRepository.save(a);

		publishAppointmentNotice(a, username, NotifSeverity.WARN, "Appointment cancelled",
				buildBody("Was scheduled for ", a));

		return new CancelAppointmentResponse();
	}

	@Override
	public GetAppointmentResponse get(GetAppointmentRequest request) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		Appointment a = appointmentRepository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.APPOINTMENT_NOT_FOUND, HttpStatus.NOT_FOUND));

		if (a.getServiceId() == null || a.getCustomerId() == null) {
			throw new ServiceException(ApiMessages.APPOINTMENT_NOT_FOUND, HttpStatus.NOT_FOUND);
		}

		ServiceItem service = serviceItemRepository.findById(a.getServiceId())
				.orElseThrow(() -> new ServiceException(ApiMessages.SERVICE_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND));

		KycCustomerRef customer = kycCustomerRefRepository.findById(a.getCustomerId())
				.orElseThrow(() -> new ServiceException(ApiMessages.CUSTOMER_NOT_FOUND, HttpStatus.NOT_FOUND));

		GetAppointmentResponse r = new GetAppointmentResponse();
		r.setId(a.getId());
		r.setCustomerId(a.getCustomerId());
		r.setCustomerName(customer.resolveDisplayName());
		r.setServiceId(a.getServiceId());
		r.setService(ServiceItemDtoMapper.toResponse(service));
		r.setTitle(a.getTitle());
		r.setStartTime(a.getStartTime());
		r.setEndTime(a.getEndTime());
		r.setStatus(a.getStatus());
		r.setNotes(a.getNotes());
		r.setCreatedAt(a.getCreatedAt());
		r.setUpdatedAt(a.getUpdatedAt());
		r.setCreatedBy(a.getCreatedBy());
		r.setUpdatedBy(a.getUpdatedBy());
		return r;
	}

	@Override
	public PageResponse<CalendarAppointmentDto> gets(GetsAppointmentsRequest request) {

		assertRangeConsistency(request.getRangeStart(), request.getRangeEnd());

		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());

		Page<Appointment> page;
		LocalDateTime rs = request.getRangeStart();
		LocalDateTime re = request.getRangeEnd();

		if (rs != null && re != null) {
			page = appointmentRepository.findIntersectingRangePagedFiltered(request.getCustomerId(), rs, re, pageable);
		} else if (request.getCustomerId() != null) {
			page = appointmentRepository.findByCustomerIdOrderByStartTimeDesc(request.getCustomerId(), pageable);
		} else {
			page = appointmentRepository.findAll(pageable);
		}

		List<CalendarAppointmentDto> items = toCalendarDtos(page.getContent());

		PageResponse<CalendarAppointmentDto> response = new PageResponse<>();
		response.setItems(items);
		response.setTotalCount(page.getTotalElements());
		response.setPageNumber(page.getNumber());
		response.setPageSize(page.getSize());
		response.setTotalPages(page.getTotalPages());
		return response;
	}

	@Override
	public List<CalendarAppointmentDto> listByRange(AppointmentsByRangeRequest request) {
		validateRange(request.getRangeStart(), request.getRangeEnd());
		return calendarForRange(request.getRangeStart(), request.getRangeEnd());
	}

	@Override
	public List<CalendarAppointmentDto> calendarForMonth(YearMonth month) {
		LocalDateTime start = month.atDay(1).atStartOfDay();
		LocalDateTime end = month.atEndOfMonth().atTime(LocalTime.MAX);
		return calendarForRange(start, end);
	}

	@Override
	public List<CalendarAppointmentDto> calendarForRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
		validateRange(rangeStart, rangeEnd);
		Long businessId = BusinessContextHolder.requireBusinessId();
		List<Appointment> list = appointmentRepository.findIntersectingRangeForBusiness(businessId, rangeStart, rangeEnd);
		return toCalendarDtos(list);
	}

	private void assertCustomerExists(Long customerId) {
		if (customerId == null) {
			throw new ServiceException(ApiMessages.CUSTOMER_NOT_FOUND, HttpStatus.BAD_REQUEST);
		}
		if (!kycCustomerRefRepository.existsById(customerId)) {
			throw new ServiceException(ApiMessages.CUSTOMER_NOT_FOUND, HttpStatus.BAD_REQUEST);
		}
	}

	private ServiceItem requireBookableService(Long serviceId) {
		if (serviceId == null) {
			throw new ServiceException(ApiMessages.SERVICE_ITEM_NOT_FOUND, HttpStatus.BAD_REQUEST);
		}
		ServiceItem service = serviceItemRepository.findById(serviceId)
				.orElseThrow(() -> new ServiceException(ApiMessages.SERVICE_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND));
		if (!Boolean.TRUE.equals(service.getActive())) {
			throw new ServiceException(ApiMessages.SERVICE_ITEM_INACTIVE, HttpStatus.BAD_REQUEST);
		}
		return service;
	}

	private void assertNoOverlapIfScheduling(Long customerId, LocalDateTime start, LocalDateTime end,
			Long excludeAppointmentId, AppointmentStatus statusAfter) {

		if (statusAfter == AppointmentStatus.CANCELLED) {
			return;
		}
		if (appointmentRepository.existsOverlapForCustomer(customerId, start, end, excludeAppointmentId)) {
			throw new ServiceException(ApiMessages.APPOINTMENT_OVERLAP, HttpStatus.CONFLICT);
		}
	}

	private static void validateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
		if (rangeStart == null || rangeEnd == null) {
			throw new ServiceException("Range start and end are required", HttpStatus.BAD_REQUEST);
		}
		if (!rangeStart.isBefore(rangeEnd)) {
			throw new ServiceException("Range start must be before range end", HttpStatus.BAD_REQUEST);
		}
	}

	private void assertRangeConsistency(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
		if (rangeStart == null ^ rangeEnd == null) {
			throw new ServiceException("Both range start and range end must be provided together", HttpStatus.BAD_REQUEST);
		}
		if (rangeStart != null && rangeEnd != null) {
			validateRange(rangeStart, rangeEnd);
		}
	}

	private List<CalendarAppointmentDto> toCalendarDtos(List<Appointment> appointments) {
		if (appointments.isEmpty()) {
			return new ArrayList<>();
		}

		Set<Long> serviceIds = new HashSet<>();
		Set<Long> customerIds = new HashSet<>();
		for (Appointment a : appointments) {
			if (a.getServiceId() != null) {
				serviceIds.add(a.getServiceId());
			}
			if (a.getCustomerId() != null) {
				customerIds.add(a.getCustomerId());
			}
		}

		Map<Long, ServiceItem> services = serviceItemRepository.findAllById(serviceIds).stream()
				.collect(Collectors.toMap(ServiceItem::getId, s -> s));

		Map<Long, KycCustomerRef> customers = kycCustomerRefRepository.findAllById(customerIds).stream()
				.collect(Collectors.toMap(KycCustomerRef::getId, c -> c, (a, b) -> a));

		List<CalendarAppointmentDto> out = new ArrayList<>(appointments.size());
		for (Appointment a : appointments) {
			CalendarAppointmentDto dto = new CalendarAppointmentDto();
			dto.setId(a.getId());
			dto.setTitle(a.getTitle());
			dto.setStartTime(a.getStartTime());
			dto.setEndTime(a.getEndTime());
			dto.setStatus(a.getStatus());
			dto.setColor(a.getStatus().calendarColor());

			ServiceItem s = services.get(a.getServiceId());
			dto.setServiceName(s != null ? s.getName() : null);

			KycCustomerRef c = customers.get(a.getCustomerId());
			dto.setCustomerName(c != null ? c.resolveDisplayName() : null);

			out.add(dto);
		}
		return out;
	}

	// Send a notification to the appointment owner and (when different) to the actor.
	private void publishAppointmentNotice(Appointment a, String actor, NotifSeverity severity, String title,
			String body) {
		if (a == null) {
			return;
		}
		String owner = a.getCreatedBy();
		String resourceId = a.getId() != null ? a.getId().toString() : null;

		notifInbox.publish(NotifPublishRequest.of(owner, NotifCategory.APPOINTMENT, severity, title)
				.body(body)
				.link(APPT_LINK)
				.resource(APPT_RESOURCE, resourceId));

		if (actor != null && !actor.trim().isEmpty() && !actor.equalsIgnoreCase(owner)) {
			notifInbox.publish(NotifPublishRequest.of(actor, NotifCategory.APPOINTMENT, severity, title)
					.body(body)
					.link(APPT_LINK)
					.resource(APPT_RESOURCE, resourceId));
		}
	}

	private static String buildBody(String prefix, Appointment a) {
		StringBuilder sb = new StringBuilder();
		if (a.getTitle() != null) {
			sb.append(a.getTitle());
		}
		if (a.getStartTime() != null) {
			if (sb.length() > 0) {
				sb.append(" — ");
			}
			sb.append(prefix);
			sb.append(a.getStartTime().toString().replace('T', ' '));
		}
		return sb.length() == 0 ? null : sb.toString();
	}

}
