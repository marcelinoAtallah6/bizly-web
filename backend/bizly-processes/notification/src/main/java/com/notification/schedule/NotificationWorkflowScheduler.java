package com.notification.schedule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.notification.repository.UmUserWelcomeQueryDao;
import com.notification.workflow.NotificationWorkflowEngine;
import com.notification.workflow.UserCreatedNotificationPayload;
import com.notification.workflow.WelcomePendingUser;

/**
 * Polls the database on a fixed delay (default every second): pending welcome emails, then birthdays today.
 * No HTTP APIs — workflows are driven only by this scheduler + DB state.
 */
@Component
public class NotificationWorkflowScheduler {

	private static final Logger log = LogManager.getLogger(NotificationWorkflowScheduler.class);

	private final UmUserWelcomeQueryDao welcomeQueryDao;
	private final NotificationWorkflowEngine workflowEngine;

	private final int maxUsersPerTick;

	public NotificationWorkflowScheduler(UmUserWelcomeQueryDao welcomeQueryDao,
			NotificationWorkflowEngine workflowEngine,
			@Value("${notification.workflow.max-users-per-tick:50}") int maxUsersPerTick) {
		this.welcomeQueryDao = welcomeQueryDao;
		this.workflowEngine = workflowEngine;
		this.maxUsersPerTick = maxUsersPerTick;
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
			workflowEngine.pollBirthdayEmails();
		} catch (Exception e) {
			log.error("[NOTIF_SCHEDULER] tick failed: {}", e.toString(), e);
		}
	}
}
