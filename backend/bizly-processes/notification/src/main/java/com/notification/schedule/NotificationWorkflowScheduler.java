package com.notification.schedule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.notification.repository.UmUserWelcomeQueryDao;
import com.notification.workflow.BroadcastDeliveryWorkflow;
import com.notification.workflow.NotificationWorkflowEngine;
import com.notification.workflow.UserCreatedNotificationPayload;
import com.notification.workflow.WelcomePendingUser;

/**
 * Polls the database on a fixed delay (default every second): pending welcome emails (users + customers) and
 * broadcast delivery. Birthday emails run on a separate daily cron. No HTTP APIs — workflows are driven only by
 * schedulers + DB state.
 */
@Component
public class NotificationWorkflowScheduler {

	private static final Logger log = LogManager.getLogger(NotificationWorkflowScheduler.class);

	private final UmUserWelcomeQueryDao welcomeQueryDao;
	private final NotificationWorkflowEngine workflowEngine;
	private final BroadcastDeliveryWorkflow broadcastDeliveryWorkflow;

	private final int maxUsersPerTick;

	private final int maxBroadcastsPerTick;

	public NotificationWorkflowScheduler(UmUserWelcomeQueryDao welcomeQueryDao,
			NotificationWorkflowEngine workflowEngine,
			BroadcastDeliveryWorkflow broadcastDeliveryWorkflow,
			@Value("${notification.workflow.max-users-per-tick:50}") int maxUsersPerTick,
			@Value("${notification.broadcast.max-broadcasts-per-tick:3}") int maxBroadcastsPerTick) {
		this.welcomeQueryDao = welcomeQueryDao;
		this.workflowEngine = workflowEngine;
		this.broadcastDeliveryWorkflow = broadcastDeliveryWorkflow;
		this.maxUsersPerTick = maxUsersPerTick;
		this.maxBroadcastsPerTick = maxBroadcastsPerTick;
	}

	@Scheduled(fixedDelayString = "${notification.workflow.poll-interval-ms:1000}")
	public void tick() {
		try {
			for (WelcomePendingUser u : welcomeQueryDao.findPendingWelcome(maxUsersPerTick)) {
				UserCreatedNotificationPayload p = new UserCreatedNotificationPayload();
				p.setUserId(u.getUserId());
				p.setUsername(u.getUsername());
				p.setEmail(u.getEmail());
				p.setFirstName(u.getFirstName());
				p.setLastName(u.getLastName());
				workflowEngine.handleUserCreatedEvent(p);
			}
			workflowEngine.pollPendingCustomerWelcomes(maxUsersPerTick);
			broadcastDeliveryWorkflow.deliverPending(maxBroadcastsPerTick);
		} catch (Exception e) {
			log.error("[NOTIF_SCHEDULER] tick failed: {}", e.toString(), e);
		}
	}

	/** User and customer birthday emails once per day (server timezone). Deduped via dispatch log. */
	@Scheduled(cron = "${notification.birthday.daily-cron:0 0 7 * * *}")
	public void dailyBirthdayEmails() {
		try {
			workflowEngine.pollBirthdayEmails();
			workflowEngine.pollCustomerBirthdayEmails();
		} catch (Exception e) {
			log.error("[NOTIF_SCHEDULER] dailyBirthdayEmails failed: {}", e.toString(), e);
		}
	}
}
