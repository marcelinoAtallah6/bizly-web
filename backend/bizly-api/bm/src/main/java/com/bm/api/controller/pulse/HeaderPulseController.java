package com.bm.api.controller.pulse;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.bm.api.dto.pulse.HeaderPulseResponse;
import com.bm.api.repository.AppointmentRepository;
import com.bm.api.repository.CustomerSaleRepository;
import com.bm.common.ApiMessages;
import com.bm.common.ApiResponse;
import com.bm.security.BusinessContextHolder;

@RestController
@RequestMapping("/header-pulse")
public class HeaderPulseController {

	private static final Logger log = LogManager.getLogger(HeaderPulseController.class);

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private CustomerSaleRepository customerSaleRepository;

	@PostMapping("/get")
	public @ResponseBody ResponseEntity<ApiResponse<HeaderPulseResponse>> get() {
		LocalDate today = LocalDate.now();
		LocalDateTime dayStart = today.atStartOfDay();
		LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime in24h = now.plusHours(24).withSecond(0).withNano(0);

		HeaderPulseResponse res = new HeaderPulseResponse();

		/*
		 * Tenant scope: business users see only their own counts. SUPER_ADMIN callers (and any caller
		 * that did NOT bring a business id, e.g. internal jobs) fall back to the global queries.
		 * Admin override (X-Business-Override) is honoured automatically because BusinessContextHolder
		 * already substitutes the override id when it is set.
		 */
		Long businessId = BusinessContextHolder.currentBusinessId();

		try {
			res.setAppointmentsToday(businessId != null
					? appointmentRepository.countByStartTimeBetweenForBusiness(businessId, dayStart, dayEnd)
					: appointmentRepository.countByStartTimeBetween(dayStart, dayEnd));
		} catch (RuntimeException ex) {
			log.warn("[HEADER_PULSE][APPT_TODAY] failed: {}", ex.getMessage());
		}
		try {
			res.setAppointmentsOpenToday(businessId != null
					? appointmentRepository.countOpenForRangeForBusiness(businessId, dayStart, dayEnd)
					: appointmentRepository.countOpenForRange(dayStart, dayEnd));
		} catch (RuntimeException ex) {
			log.warn("[HEADER_PULSE][APPT_OPEN_TODAY] failed: {}", ex.getMessage());
		}
		try {
			res.setAppointmentsUpcoming24h(businessId != null
					? appointmentRepository.countUpcomingForBusiness(businessId, now, in24h)
					: appointmentRepository.countUpcoming(now, in24h));
		} catch (RuntimeException ex) {
			log.warn("[HEADER_PULSE][APPT_UPCOMING] failed: {}", ex.getMessage());
		}
		try {
			res.setSalesCountToday(businessId != null
					? customerSaleRepository.countForRangeForBusiness(businessId, dayStart, dayEnd)
					: customerSaleRepository.countForRange(dayStart, dayEnd));
			Double amt = businessId != null
					? customerSaleRepository.sumTotalForRangeForBusiness(businessId, dayStart, dayEnd)
					: customerSaleRepository.sumTotalForRange(dayStart, dayEnd);
			res.setSalesAmountToday(amt != null ? amt : 0.0);
		} catch (RuntimeException ex) {
			log.warn("[HEADER_PULSE][SALES] failed: {}", ex.getMessage());
		}

		return ResponseEntity.ok(ApiResponse.success(res, ApiMessages.SUCCESS));
	}
}
