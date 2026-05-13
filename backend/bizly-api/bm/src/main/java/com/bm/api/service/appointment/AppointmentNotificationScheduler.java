package com.bm.api.service.appointment;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bm.api.model.Appointment;
import com.bm.api.repository.AppointmentRepository;
import com.bm.api.service.notif.INotifInboxService;
import com.bm.api.service.notif.NotifCategory;
import com.bm.api.service.notif.NotifPublishRequest;
import com.bm.api.service.notif.NotifSeverity;

/**
 * Time-driven notifications for appointments. Producers in this class are
 * <strong>idempotent</strong>: every publish is keyed on a stable
 * {@code (resourceType, resourceId)} so re-runs or overlapping windows never
 * spam the user.
 *
 * <p>Active jobs:
 * <ul>
 *   <li>{@link #publish60MinReminders()} – every 10 minutes, 50–70 min ahead.</li>
 *   <li>{@link #publishPastDueAlerts()} – every 30 minutes, end_time &lt; now.</li>
 *   <li>{@link #publishMorningBrief()} – daily at 08:00, one summary per user.</li>
 * </ul>
 *
 * <p>Each method swallows its own exceptions so one bad row never disables the
 * scheduler — failures are logged and the next tick will retry.
 */
@Component
public class AppointmentNotificationScheduler {

	private static final Logger log = LogManager.getLogger(AppointmentNotificationScheduler.class);

	private static final String LINK = "/bm/appointments";

	// Stable (per-event) resourceType discriminators for dedup.
	private static final String RT_REMINDER_60 = "APPOINTMENT_REMINDER_60";
	private static final String RT_PASTDUE = "APPOINTMENT_PASTDUE";
	private static final String RT_MORNING_BRIEF = "APPOINTMENT_MORNING_BRIEF";

	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter DAY_KEY = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter SLOT_KEY = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

	@Autowired
	private AppointmentRepository appointmentRepository;

	@Autowired
	private INotifInboxService notifInbox;

	// ---------------------------------------------------------------------------
	// 60-minute reminder
	//
	// Cron runs every 10 min; the SQL window is 50-70 min ahead. The resourceId
	// embeds the start_time slot, so if the appointment is rescheduled the
	// reminder will fire again for the new slot.
	// ---------------------------------------------------------------------------
	@Scheduled(cron = "0 */10 * * * *")
	public void publish60MinReminders() {
		try {
			LocalDateTime now = LocalDateTime.now();
			LocalDateTime from = now.plusMinutes(50);
			LocalDateTime to = now.plusMinutes(70);

			List<Appointment> candidates = appointmentRepository.findOpenStartingBetween(from, to);
			if (candidates.isEmpty()) {
				return;
			}

			int sent = 0;
			for (Appointment a : candidates) {
				String owner = a.getCreatedBy();
				if (owner == null || owner.isBlank() || a.getId() == null || a.getStartTime() == null) {
					continue;
				}
				String resourceId = a.getId() + "@" + a.getStartTime().format(SLOT_KEY);

				long minutes = Duration.between(now, a.getStartTime()).toMinutes();
				String title = "Appointment in " + Math.max(minutes, 1) + " min";
				String body = appointmentSummaryLine(a);

				boolean wrote = notifInbox.publishOnce(NotifPublishRequest
						.of(owner, NotifCategory.APPOINTMENT, NotifSeverity.WARN, title)
						.body(body)
						.link(LINK)
						.resource(RT_REMINDER_60, resourceId));
				if (wrote) {
					sent++;
				}
			}
			if (sent > 0) {
				log.info("[NOTIF_SCHED][REMINDER_60] candidates={} sent={}", candidates.size(), sent);
			}
		} catch (RuntimeException ex) {
			log.warn("[NOTIF_SCHED][REMINDER_60][FAIL] {}", ex.getMessage(), ex);
		}
	}

	// ---------------------------------------------------------------------------
	// Past-due alert
	//
	// Every 30 min, scan open appointments whose end_time is already in the past.
	// One notification per appointment (resourceId = appointment id without slot)
	// so each past-due appointment alerts the owner exactly once until they act.
	// ---------------------------------------------------------------------------
	@Scheduled(cron = "0 */30 * * * *")
	public void publishPastDueAlerts() {
		try {
			LocalDateTime now = LocalDateTime.now();
			List<Appointment> overdue = appointmentRepository.findPastDueOpen(now);
			if (overdue.isEmpty()) {
				return;
			}

			int sent = 0;
			for (Appointment a : overdue) {
				String owner = a.getCreatedBy();
				if (owner == null || owner.isBlank() || a.getId() == null) {
					continue;
				}
				String resourceId = Long.toString(a.getId());

				String title = "Appointment past due";
				String body = appointmentSummaryLine(a) + " — mark complete or no-show.";

				boolean wrote = notifInbox.publishOnce(NotifPublishRequest
						.of(owner, NotifCategory.APPOINTMENT, NotifSeverity.WARN, title)
						.body(body)
						.link(LINK)
						.resource(RT_PASTDUE, resourceId));
				if (wrote) {
					sent++;
				}
			}
			if (sent > 0) {
				log.info("[NOTIF_SCHED][PASTDUE] overdue={} sent={}", overdue.size(), sent);
			}
		} catch (RuntimeException ex) {
			log.warn("[NOTIF_SCHED][PASTDUE][FAIL] {}", ex.getMessage(), ex);
		}
	}

	// ---------------------------------------------------------------------------
	// Morning brief
	//
	// 08:00 every day: aggregate today's open appointments per createdBy user
	// and send a single summary row. Dedup key is yyyyMMdd of "today", so re-runs
	// on the same day don't duplicate the message even if the host restarts.
	// ---------------------------------------------------------------------------
	@Scheduled(cron = "0 0 8 * * *")
	public void publishMorningBrief() {
		try {
			LocalDate today = LocalDate.now();
			LocalDateTime dayStart = today.atStartOfDay();
			LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

			List<Appointment> dayList = appointmentRepository.findOpenForDay(dayStart, dayEnd);
			if (dayList.isEmpty()) {
				return;
			}

			Map<String, List<Appointment>> grouped = groupByOwner(dayList);
			String dayKey = today.format(DAY_KEY);

			int sent = 0;
			for (Map.Entry<String, List<Appointment>> entry : grouped.entrySet()) {
				String owner = entry.getKey();
				List<Appointment> mine = entry.getValue();
				if (owner == null || owner.isBlank() || mine.isEmpty()) {
					continue;
				}

				Appointment first = mine.get(0); // already ordered by start_time
				String title = mine.size() == 1
						? "1 appointment today"
						: mine.size() + " appointments today";
				String body = "First at " + first.getStartTime().format(TIME);
				if (first.getTitle() != null && !first.getTitle().isBlank()) {
					body += " · " + first.getTitle();
				}

				boolean wrote = notifInbox.publishOnce(NotifPublishRequest
						.of(owner, NotifCategory.APPOINTMENT, NotifSeverity.INFO, title)
						.body(body)
						.link(LINK)
						.resource(RT_MORNING_BRIEF, dayKey));
				if (wrote) {
					sent++;
				}
			}
			log.info("[NOTIF_SCHED][MORNING_BRIEF] day={} users={} sent={}", dayKey, grouped.size(), sent);
		} catch (RuntimeException ex) {
			log.warn("[NOTIF_SCHED][MORNING_BRIEF][FAIL] {}", ex.getMessage(), ex);
		}
	}

	// ---------------------------------------------------------------------------
	// Helpers
	// ---------------------------------------------------------------------------

	private static Map<String, List<Appointment>> groupByOwner(List<Appointment> list) {
		// Preserve order so the first appointment for each owner is the earliest.
		Map<String, List<Appointment>> map = new LinkedHashMap<>();
		for (Appointment a : list) {
			String owner = a.getCreatedBy();
			if (owner == null) {
				continue;
			}
			map.computeIfAbsent(owner, k -> new java.util.ArrayList<>()).add(a);
		}
		return map;
	}

	private static String appointmentSummaryLine(Appointment a) {
		StringBuilder sb = new StringBuilder();
		if (a.getTitle() != null) {
			sb.append(a.getTitle());
		}
		if (a.getStartTime() != null) {
			if (sb.length() > 0) {
				sb.append(" · ");
			}
			sb.append("at ").append(a.getStartTime().format(TIME));
		}
		return sb.toString();
	}
}
