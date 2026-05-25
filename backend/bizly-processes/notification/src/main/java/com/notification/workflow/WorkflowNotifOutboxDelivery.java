package com.notification.workflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.domain.NotificationDispatchLogEntity;
import com.notification.domain.NotificationTemplateEntity;
import com.notification.repository.NotifOutboxQueryDao;
import com.notification.repository.NotifOutboxQueryDao.PendingOutboxRow;
import com.notification.repository.NotificationDispatchLogRepository;
import com.notification.repository.NotificationTemplateRepository;

/**
 * Delivers emails queued by the generic workflow engine ({@code UM_NOTIF_OUTBOX}).
 */
@Service
public class WorkflowNotifOutboxDelivery {

	private static final Logger log = LogManager.getLogger(WorkflowNotifOutboxDelivery.class);

	private final NotifOutboxQueryDao outboxQueryDao;
	private final NotificationTemplateRepository templateRepository;
	private final NotificationDispatchLogRepository dispatchLogRepository;
	private final TemplatePlaceholderRenderer renderer;
	private final OutboundMailService mailService;
	private final ObjectMapper objectMapper;

	public WorkflowNotifOutboxDelivery(NotifOutboxQueryDao outboxQueryDao,
			NotificationTemplateRepository templateRepository,
			NotificationDispatchLogRepository dispatchLogRepository,
			TemplatePlaceholderRenderer renderer,
			OutboundMailService mailService,
			ObjectMapper objectMapper) {
		this.outboxQueryDao = outboxQueryDao;
		this.templateRepository = templateRepository;
		this.dispatchLogRepository = dispatchLogRepository;
		this.renderer = renderer;
		this.mailService = mailService;
		this.objectMapper = objectMapper;
	}

	public void deliverPending(int maxPerTick) {
		List<PendingOutboxRow> rows = outboxQueryDao.findPending(maxPerTick);
		for (PendingOutboxRow row : rows) {
			if (outboxQueryDao.markProcessing(row.getId()) == 0) {
				continue;
			}
			deliverOne(row);
		}
	}

	private void deliverOne(PendingOutboxRow row) {
		String channel = row.getChannel() != null ? row.getChannel().toUpperCase() : "EMAIL";
		if ("INBOX".equals(channel)) {
			outboxQueryDao.markSent(row.getId());
			log.info("[WF_OUTBOX] INBOX channel not yet implemented — marked sent id={}", row.getId());
			return;
		}
		if ("BOTH".equals(channel)) {
			channel = "EMAIL";
		}
		if (!"EMAIL".equals(channel)) {
			outboxQueryDao.markFailed(row.getId(), "Unsupported channel: " + channel);
			return;
		}

		if (row.getRecipientEmail() == null || row.getRecipientEmail().isBlank()) {
			outboxQueryDao.markFailed(row.getId(), "Recipient email missing");
			appendLog(row, NotificationConstants.STATUS_FAILED, "Recipient email missing");
			return;
		}

		Optional<NotificationTemplateEntity> tmpl = templateRepository
				.findByTemplateKeyAndActiveInd(row.getTemplateKey(), NotificationConstants.ACTIVE_YES);
		if (tmpl.isEmpty()) {
			outboxQueryDao.markFailed(row.getId(), "Template not found: " + row.getTemplateKey());
			appendLog(row, NotificationConstants.STATUS_FAILED, "Template not found or inactive");
			return;
		}

		Map<String, String> vars = parseContextVars(row.getContextJson());
		String subject = renderer.render(tmpl.get().getSubjectTemplate(), vars);
		String html = renderer.render(tmpl.get().getHtmlBody(), vars);
		String text = renderer.render(tmpl.get().getTextBody(), vars);

		try {
			mailService.sendHtmlEmail(row.getRecipientEmail(), subject, html, text);
			outboxQueryDao.markSent(row.getId());
			appendLog(row, NotificationConstants.STATUS_SUCCESS, null);
			log.info("[WF_OUTBOX] sent action={} template={} to={}", row.getActionCode(), row.getTemplateKey(),
					row.getRecipientEmail());
		} catch (Exception ex) {
			String err = ex.getMessage();
			outboxQueryDao.markFailed(row.getId(), err);
			appendLog(row, NotificationConstants.STATUS_FAILED, err);
			log.error("[WF_OUTBOX] send failed id={}: {}", row.getId(), err);
		}
	}

	private Map<String, String> parseContextVars(String json) {
		if (json == null || json.isBlank()) {
			return new HashMap<>();
		}
		try {
			Map<String, String> raw = objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
			Map<String, String> vars = new HashMap<>();
			raw.forEach((k, v) -> vars.put(k, v != null ? v : ""));
			return vars;
		} catch (Exception e) {
			return new HashMap<>();
		}
	}

	private void appendLog(PendingOutboxRow row, String status, String error) {
		NotificationDispatchLogEntity logRow = new NotificationDispatchLogEntity();
		logRow.setProcessType("WF_" + row.getActionCode());
		logRow.setTemplateKey(row.getTemplateKey());
		logRow.setUserId(row.getRecipientId());
		logRow.setRecipientEmail(row.getRecipientEmail());
		logRow.setStatus(status);
		logRow.setErrorDetail(error);
		dispatchLogRepository.save(logRow);
	}
}
