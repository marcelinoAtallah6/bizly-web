package com.travel.api.service.booking;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.travel.api.service.notif.INotifInboxService;
import com.travel.api.service.notif.NotifPublishRequest;
import com.travel.api.service.notif.TravelNotificationRecipientService;

@Component
public class TravelBookingNotificationPublisher {

	static final String LINK = "/travel/bookings";
	static final String RT_BOOKING = "TRAVEL_BOOKING";
	static final String RT_REMINDER_7D = "TRAVEL_BOOKING_REMINDER_7D";
	static final String RT_PASTDUE = "TRAVEL_BOOKING_PASTDUE";
	static final String RT_MORNING_BRIEF = "TRAVEL_BOOKING_MORNING_BRIEF";

	@Autowired
	private INotifInboxService notifInbox;

	@Autowired
	private TravelNotificationRecipientService recipients;

	public int publishOnceToTravelTeam(Long businessId, NotifPublishRequest template) {
		return fanOut(businessId, template, true);
	}

	public int publishToTravelTeam(Long businessId, NotifPublishRequest template) {
		return fanOut(businessId, template, false);
	}

	private int fanOut(Long businessId, NotifPublishRequest template, boolean once) {
		if (businessId == null || template == null) {
			return 0;
		}
		List<String> usernames = recipients.findUsernamesWithBookingsView(businessId);
		if (usernames.isEmpty()) {
			return 0;
		}
		int sent = 0;
		for (String username : usernames) {
			NotifPublishRequest req = NotifPublishRequest
					.of(username, template.getCategory(), template.getSeverity(), template.getTitle())
					.body(template.getBody())
					.link(template.getLinkRoute())
					.resource(template.getResourceType(), template.getResourceId())
					.businessId(businessId);
			boolean wrote = once ? notifInbox.publishOnce(req) : notifInbox.publish(req);
			if (wrote) {
				sent++;
			}
		}
		return sent;
	}

	public static String bookingSummaryLine(String referenceNo, java.time.LocalDate departureDate) {
		StringBuilder sb = new StringBuilder();
		if (referenceNo != null && !referenceNo.isBlank()) {
			sb.append(referenceNo.trim());
		}
		if (departureDate != null) {
			if (sb.length() > 0) {
				sb.append(" · ");
			}
			sb.append("departs ").append(departureDate);
		}
		return sb.toString();
	}
}
