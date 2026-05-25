package com.notification.schedule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.notification.workflow.BroadcastDeliveryWorkflow;
import com.notification.workflow.WorkflowNotifOutboxDelivery;

/**
 * Delivers emails queued by the workflow engine ({@code UM_NOTIF_OUTBOX}).
 * Domain triggers, scheduled jobs (birthdays), and gateway hooks all enqueue via UM orchestrator.
 */
@Component
public class NotificationWorkflowScheduler {

	private static final Logger log = LogManager.getLogger(NotificationWorkflowScheduler.class);

	private final BroadcastDeliveryWorkflow broadcastDeliveryWorkflow;
	private final WorkflowNotifOutboxDelivery workflowOutboxDelivery;

	private final int maxOutboxPerTick;
	private final int maxBroadcastsPerTick;

	public NotificationWorkflowScheduler(BroadcastDeliveryWorkflow broadcastDeliveryWorkflow,
			WorkflowNotifOutboxDelivery workflowOutboxDelivery,
			@Value("${notification.workflow.max-outbox-per-tick:50}") int maxOutboxPerTick,
			@Value("${notification.broadcast.max-broadcasts-per-tick:3}") int maxBroadcastsPerTick) {
		this.broadcastDeliveryWorkflow = broadcastDeliveryWorkflow;
		this.workflowOutboxDelivery = workflowOutboxDelivery;
		this.maxOutboxPerTick = maxOutboxPerTick;
		this.maxBroadcastsPerTick = maxBroadcastsPerTick;
	}

	@Scheduled(fixedDelayString = "${notification.workflow.poll-interval-ms:1000}")
	public void tick() {
		try {
			workflowOutboxDelivery.deliverPending(maxOutboxPerTick);
			broadcastDeliveryWorkflow.deliverPending(maxBroadcastsPerTick);
		} catch (Exception e) {
			log.error("[NOTIF_SCHEDULER] tick failed: {}", e.toString(), e);
		}
	}
}
