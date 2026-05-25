package com.travel.api.service.booking;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.travel.api.model.TravelBooking;
import com.travel.api.repository.TravelBookingRepository;
import com.travel.api.service.notif.NotifCategory;
import com.travel.api.service.notif.NotifPublishRequest;
import com.travel.api.service.notif.NotifSeverity;

/**
 * Time-driven travel booking notifications for users with VIEW on {@code /travel/bookings}.
 */
@Component
public class TravelBookingNotificationScheduler {

	private static final Logger log = LogManager.getLogger(TravelBookingNotificationScheduler.class);

	private static final DateTimeFormatter DAY_KEY = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Autowired
	private TravelBookingRepository bookingRepository;

	@Autowired
	private TravelBookingNotificationPublisher publisher;

	/** Daily reminder for departures exactly 7 days ahead. */
	@Scheduled(cron = "0 0 9 * * *")
	public void publish7DayReminders() {
		try {
			LocalDate target = LocalDate.now().plusDays(7);
			List<TravelBooking> candidates = bookingRepository.findOpenDeparturesOnDate(target);
			if (candidates.isEmpty()) {
				return;
			}
			int sent = 0;
			for (TravelBooking b : candidates) {
				if (b.getId() == null || b.getBusinessId() == null) {
					continue;
				}
				String resourceId = b.getId() + "@" + target.format(DAY_KEY);
				String title = "Booking departs in 7 days";
				String body = TravelBookingNotificationPublisher.bookingSummaryLine(b.getReferenceNo(), b.getDepartureDate());
				int n = publisher.publishOnceToTravelTeam(b.getBusinessId(),
						NotifPublishRequest.of(null, NotifCategory.TRAVEL_BOOKING, NotifSeverity.WARN, title)
								.body(body)
								.link(linkFor(b.getId()))
								.resource(TravelBookingNotificationPublisher.RT_REMINDER_7D, resourceId)
								.businessId(b.getBusinessId()));
				sent += n;
			}
			if (sent > 0) {
				log.info("[NOTIF_SCHED][TRAVEL_REMINDER_7D] candidates={} rowsSent={}", candidates.size(), sent);
			}
		} catch (RuntimeException ex) {
			log.warn("[NOTIF_SCHED][TRAVEL_REMINDER_7D][FAIL] {}", ex.getMessage(), ex);
		}
	}

	/** Open bookings whose departure date is already in the past. */
	@Scheduled(cron = "0 0 */6 * * *")
	public void publishPastDueAlerts() {
		try {
			LocalDate today = LocalDate.now();
			List<TravelBooking> overdue = bookingRepository.findPastDueOpen(today);
			if (overdue.isEmpty()) {
				return;
			}
			int sent = 0;
			for (TravelBooking b : overdue) {
				if (b.getId() == null || b.getBusinessId() == null) {
					continue;
				}
				String resourceId = Long.toString(b.getId());
				String title = "Booking past departure";
				String body = TravelBookingNotificationPublisher.bookingSummaryLine(b.getReferenceNo(), b.getDepartureDate())
						+ " — update status or follow up.";
				int n = publisher.publishOnceToTravelTeam(b.getBusinessId(),
						NotifPublishRequest.of(null, NotifCategory.TRAVEL_BOOKING, NotifSeverity.WARN, title)
								.body(body)
								.link(linkFor(b.getId()))
								.resource(TravelBookingNotificationPublisher.RT_PASTDUE, resourceId)
								.businessId(b.getBusinessId()));
				sent += n;
			}
			if (sent > 0) {
				log.info("[NOTIF_SCHED][TRAVEL_PASTDUE] overdue={} rowsSent={}", overdue.size(), sent);
			}
		} catch (RuntimeException ex) {
			log.warn("[NOTIF_SCHED][TRAVEL_PASTDUE][FAIL] {}", ex.getMessage(), ex);
		}
	}

	/** 08:00 daily summary of today's departures per business (one row per travel user). */
	@Scheduled(cron = "0 0 8 * * *")
	public void publishMorningBrief() {
		try {
			LocalDate today = LocalDate.now();
			List<TravelBooking> dayList = bookingRepository.findOpenDeparturesOnDate(today);
			if (dayList.isEmpty()) {
				return;
			}
			Map<Long, List<TravelBooking>> grouped = groupByBusiness(dayList);
			String dayKey = today.format(DAY_KEY);
			int sent = 0;
			for (Map.Entry<Long, List<TravelBooking>> entry : grouped.entrySet()) {
				Long businessId = entry.getKey();
				List<TravelBooking> mine = entry.getValue();
				if (businessId == null || mine.isEmpty()) {
					continue;
				}
				TravelBooking first = mine.get(0);
				String title = mine.size() == 1 ? "1 departure today" : mine.size() + " departures today";
				String body = "First: "
						+ TravelBookingNotificationPublisher.bookingSummaryLine(first.getReferenceNo(), first.getDepartureDate());
				int n = publisher.publishOnceToTravelTeam(businessId,
						NotifPublishRequest.of(null, NotifCategory.TRAVEL_BOOKING, NotifSeverity.INFO, title)
								.body(body)
								.link(TravelBookingNotificationPublisher.LINK)
								.resource(TravelBookingNotificationPublisher.RT_MORNING_BRIEF, dayKey)
								.businessId(businessId));
				sent += n;
			}
			log.info("[NOTIF_SCHED][TRAVEL_MORNING_BRIEF] day={} businesses={} rowsSent={}", dayKey, grouped.size(), sent);
		} catch (RuntimeException ex) {
			log.warn("[NOTIF_SCHED][TRAVEL_MORNING_BRIEF][FAIL] {}", ex.getMessage(), ex);
		}
	}

	private static Map<Long, List<TravelBooking>> groupByBusiness(List<TravelBooking> list) {
		Map<Long, List<TravelBooking>> map = new LinkedHashMap<>();
		for (TravelBooking b : list) {
			if (b.getBusinessId() == null) {
				continue;
			}
			map.computeIfAbsent(b.getBusinessId(), k -> new ArrayList<>()).add(b);
		}
		return map;
	}

	private static String linkFor(Long bookingId) {
		return bookingId != null ? TravelBookingNotificationPublisher.LINK + "?id=" + bookingId
				: TravelBookingNotificationPublisher.LINK;
	}
}
