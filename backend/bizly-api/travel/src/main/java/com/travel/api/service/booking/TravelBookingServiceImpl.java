package com.travel.api.service.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.travel.api.dto.booking.TravelBookingDtos.*;
import com.travel.api.model.TravelBooking;
import com.travel.api.model.TravelPackage;
import com.travel.api.repository.TravelBookingRepository;
import com.travel.api.repository.TravelClientRepository;
import com.travel.api.repository.TravelPackageRepository;
import com.travel.api.service.notif.NotifCategory;
import com.travel.api.service.notif.NotifPublishRequest;
import com.travel.api.service.notif.NotifSeverity;
import com.travel.common.ApiMessages;
import com.travel.common.PageResponse;
import com.travel.exception.ServiceException;
import com.travel.security.BusinessContextHolder;

@Service
public class TravelBookingServiceImpl implements ITravelBookingService {

	@Autowired
	private TravelBookingRepository repository;

	@Autowired
	private TravelClientRepository clientRepository;

	@Autowired
	private TravelPackageRepository packageRepository;

	@Autowired
	private TravelBookingNotificationPublisher bookingNotifications;

	@Override
	public AddTravelBookingResponse add(AddTravelBookingRequest request, String username) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		clientRepository.findByIdAndBusinessId(request.getClientId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.CLIENT_NOT_FOUND, HttpStatus.NOT_FOUND));
		TravelBooking e = new TravelBooking();
		e.setBusinessId(businessId);
		e.setClientId(request.getClientId());
		e.setPackageId(request.getPackageId());
		e.setReferenceNo(request.getReferenceNo());
		e.setStatus(request.getStatus() != null ? request.getStatus() : "DRAFT");
		e.setDepartureDate(request.getDepartureDate());
		e.setReturnDate(request.getReturnDate());
		e.setTotalAmount(request.getTotalAmount());
		e.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
		e.setNotes(request.getNotes());
		applyWorkflowFields(e, request.getTimelineStage(), request.getApprovalStatus(),
				request.getPaymentSchedule(), request.getRequiresApproval());
		LocalDateTime now = LocalDateTime.now();
		e.setCreatedAt(now);
		e.setUpdatedAt(now);
		e.setCreatedBy(username);
		e.setUpdatedBy(username);
		repository.save(e);
		publishBookingNotice(e, username, NotifSeverity.SUCCESS, "Booking created",
				buildBookingBody("New booking", e));
		AddTravelBookingResponse res = new AddTravelBookingResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public UpdateTravelBookingResponse update(UpdateTravelBookingRequest request, String username) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelBooking e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.BOOKING_NOT_FOUND, HttpStatus.NOT_FOUND));
		clientRepository.findByIdAndBusinessId(request.getClientId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.CLIENT_NOT_FOUND, HttpStatus.NOT_FOUND));
		e.setClientId(request.getClientId());
		e.setPackageId(request.getPackageId());
		e.setReferenceNo(request.getReferenceNo());
		if (request.getStatus() != null) {
			e.setStatus(request.getStatus());
		}
		e.setDepartureDate(request.getDepartureDate());
		e.setReturnDate(request.getReturnDate());
		e.setTotalAmount(request.getTotalAmount());
		if (request.getCurrency() != null) {
			e.setCurrency(request.getCurrency());
		}
		e.setNotes(request.getNotes());
		applyWorkflowFields(e, request.getTimelineStage(), request.getApprovalStatus(),
				request.getPaymentSchedule(), request.getRequiresApproval());
		e.setUpdatedAt(LocalDateTime.now());
		e.setUpdatedBy(username);
		repository.save(e);
		NotifSeverity sev = "CANCELLED".equalsIgnoreCase(e.getStatus()) ? NotifSeverity.WARN : NotifSeverity.INFO;
		String title = "CANCELLED".equalsIgnoreCase(e.getStatus()) ? "Booking cancelled" : "Booking updated";
		publishBookingNotice(e, username, sev, title, buildBookingBody(title, e));
		UpdateTravelBookingResponse res = new UpdateTravelBookingResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public DeleteTravelBookingResponse delete(DeleteTravelBookingRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelBooking e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.BOOKING_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long deletedId = e.getId();
		publishBookingNotice(e, e.getUpdatedBy(), NotifSeverity.WARN, "Booking deleted",
				buildBookingBody("Removed", e));
		repository.delete(e);
		DeleteTravelBookingResponse res = new DeleteTravelBookingResponse();
		res.setId(deletedId);
		return res;
	}

	@Override
	public GetTravelBookingResponse get(GetTravelBookingRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelBooking e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.BOOKING_NOT_FOUND, HttpStatus.NOT_FOUND));
		return toResponse(e);
	}

	@Override
	public PageResponse<GetTravelBookingResponse> gets(GetsTravelBookingsRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<TravelBooking> page = repository.findAllByBusinessId(businessId, pageable);
		List<GetTravelBookingResponse> items = page.getContent().stream().map(this::toResponse)
				.collect(Collectors.toList());
		PageResponse<GetTravelBookingResponse> res = new PageResponse<>();
		res.setItems(items);
		res.setTotalCount(page.getTotalElements());
		res.setPageNumber(page.getNumber());
		res.setPageSize(page.getSize());
		res.setTotalPages(page.getTotalPages());
		return res;
	}

	@Override
	public BookingAvailabilityResponse availability(BookingAvailabilityRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		YearMonth ym = YearMonth.of(request.getYear(), request.getMonth());
		LocalDate rangeStart = ym.atDay(1);
		LocalDate rangeEnd = ym.atEndOfMonth();

		boolean packageUnavailable = false;
		if (request.getPackageId() != null) {
			TravelPackage pkg = packageRepository.findByIdAndBusinessId(request.getPackageId(), businessId)
					.orElse(null);
			packageUnavailable = pkg == null || Boolean.FALSE.equals(pkg.getActive());
		}

		List<TravelBooking> bookings = repository.findOverlappingInRange(businessId, request.getPackageId(),
				rangeStart, rangeEnd);

		BookingAvailabilityResponse res = new BookingAvailabilityResponse();
		List<BookingAvailabilityDayResponse> days = new ArrayList<>();
		for (int d = 1; d <= ym.lengthOfMonth(); d++) {
			LocalDate day = ym.atDay(d);
			BookingAvailabilityDayResponse row = new BookingAvailabilityDayResponse();
			row.setDate(day);
			if (packageUnavailable) {
				row.setStatus("UNAVAILABLE");
			} else {
				row.setStatus(resolveDayStatus(day, bookings, request.getExcludeBookingId()));
			}
			days.add(row);
		}
		res.setDays(days);
		return res;
	}

	private String resolveDayStatus(LocalDate day, List<TravelBooking> bookings, Long excludeBookingId) {
		String worst = "AVAILABLE";
		for (TravelBooking b : bookings) {
			if (excludeBookingId != null && excludeBookingId.equals(b.getId())) {
				continue;
			}
			if (!overlapsDay(b, day)) {
				continue;
			}
			String st = b.getStatus() != null ? b.getStatus().toUpperCase() : "";
			if ("CANCELLED".equals(st)) {
				continue;
			}
			if ("CONFIRMED".equals(st) || "COMPLETED".equals(st)) {
				return "BOOKED";
			}
			if ("ENQUIRY".equals(st) || "QUOTED".equals(st) || "DRAFT".equals(st)) {
				worst = "PENDING";
			}
		}
		return worst;
	}

	private boolean overlapsDay(TravelBooking b, LocalDate day) {
		if (b.getDepartureDate() == null) {
			return false;
		}
		LocalDate end = b.getReturnDate() != null ? b.getReturnDate() : b.getDepartureDate();
		return !day.isBefore(b.getDepartureDate()) && !day.isAfter(end);
	}

	private void applyWorkflowFields(TravelBooking e, String timelineStage, String approvalStatus,
			java.util.List<com.travel.api.dto.packagepkg.TravelPackageDetailJson.PaymentScheduleItemDto> paymentSchedule,
			Boolean requiresApproval) {
		if (timelineStage != null) {
			e.setTimelineStage(timelineStage);
		}
		if (approvalStatus != null) {
			e.setApprovalStatus(approvalStatus);
		}
		if (paymentSchedule != null) {
			e.setPaymentScheduleJson(TravelBookingJsonMapper.toPaymentScheduleJson(paymentSchedule));
		}
		if (requiresApproval != null) {
			e.setRequiresApproval(requiresApproval);
		}
	}

	private GetTravelBookingResponse toResponse(TravelBooking e) {
		GetTravelBookingResponse r = new GetTravelBookingResponse();
		r.setId(e.getId());
		r.setClientId(e.getClientId());
		r.setPackageId(e.getPackageId());
		r.setReferenceNo(e.getReferenceNo());
		r.setStatus(e.getStatus());
		r.setDepartureDate(e.getDepartureDate());
		r.setReturnDate(e.getReturnDate());
		r.setTotalAmount(e.getTotalAmount());
		r.setCurrency(e.getCurrency());
		r.setNotes(e.getNotes());
		r.setTimelineStage(e.getTimelineStage());
		r.setApprovalStatus(e.getApprovalStatus());
		r.setPaymentSchedule(TravelBookingJsonMapper.fromPaymentScheduleJson(e.getPaymentScheduleJson()));
		r.setRequiresApproval(e.getRequiresApproval());
		return r;
	}

	private void publishBookingNotice(TravelBooking b, String actor, NotifSeverity severity, String title,
			String body) {
		if (b == null || b.getBusinessId() == null) {
			return;
		}
		String resourceId = b.getId() != null ? b.getId().toString() : null;
		String link = b.getId() != null ? TravelBookingNotificationPublisher.LINK + "?id=" + b.getId()
				: TravelBookingNotificationPublisher.LINK;
		bookingNotifications.publishToTravelTeam(b.getBusinessId(),
				NotifPublishRequest.of(null, NotifCategory.TRAVEL_BOOKING, severity, title)
						.body(body)
						.link(link)
						.resource(TravelBookingNotificationPublisher.RT_BOOKING, resourceId)
						.businessId(b.getBusinessId()));
	}

	private static String buildBookingBody(String prefix, TravelBooking b) {
		StringBuilder sb = new StringBuilder();
		if (prefix != null && !prefix.isBlank()) {
			sb.append(prefix.trim());
		}
		String summary = TravelBookingNotificationPublisher.bookingSummaryLine(b.getReferenceNo(), b.getDepartureDate());
		if (!summary.isEmpty()) {
			if (sb.length() > 0) {
				sb.append(": ");
			}
			sb.append(summary);
		}
		if (b.getStatus() != null && !b.getStatus().isBlank()) {
			if (sb.length() > 0) {
				sb.append(" · ");
			}
			sb.append(b.getStatus().trim());
		}
		return sb.toString();
	}
}
