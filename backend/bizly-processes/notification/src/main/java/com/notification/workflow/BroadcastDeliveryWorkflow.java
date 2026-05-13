package com.notification.workflow;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.domain.BroadcastMessageEntity;
import com.notification.domain.BroadcastRecipientEntity;
import com.notification.domain.NotificationDispatchLogEntity;
import com.notification.repository.BroadcastMessageRepository;
import com.notification.repository.BroadcastRecipientRepository;
import com.notification.repository.BroadcastRecipientResolutionDao;
import com.notification.repository.NotificationDispatchLogRepository;

/**
 * Picks queued broadcast rows and sends email batches. Idempotent per (broadcast, email) via
 * {@code NOTIF_BROADCAST_RCPT}.
 */
@Service
public class BroadcastDeliveryWorkflow {

	private static final Logger log = LogManager.getLogger(BroadcastDeliveryWorkflow.class);

	public static final String TARGET_ALL_USERS = "ALL_USERS";
	public static final String TARGET_ALL_CUSTOMERS = "ALL_CUSTOMERS";
	public static final String TARGET_ROLE_BASED = "ROLE_BASED";
	public static final String TARGET_CUSTOM_SEGMENT = "CUSTOM_SEGMENT";

	private final BroadcastMessageRepository broadcastMessageRepository;
	private final BroadcastRecipientRepository broadcastRecipientRepository;
	private final BroadcastRecipientResolutionDao resolutionDao;
	private final TemplatePlaceholderRenderer renderer;
	private final OutboundMailService mailService;
	private final NotificationDispatchLogRepository dispatchLogRepository;
	private final ObjectMapper objectMapper;

	private final int maxRecipientsPerRun;

	private final TransactionTemplate transactionTemplate;

	public BroadcastDeliveryWorkflow(BroadcastMessageRepository broadcastMessageRepository,
			BroadcastRecipientRepository broadcastRecipientRepository,
			BroadcastRecipientResolutionDao resolutionDao,
			TemplatePlaceholderRenderer renderer,
			OutboundMailService mailService,
			NotificationDispatchLogRepository dispatchLogRepository,
			ObjectMapper objectMapper,
			PlatformTransactionManager transactionManager,
			@Value("${notification.broadcast.max-recipients-per-run:500}") int maxRecipientsPerRun) {
		this.broadcastMessageRepository = broadcastMessageRepository;
		this.broadcastRecipientRepository = broadcastRecipientRepository;
		this.resolutionDao = resolutionDao;
		this.renderer = renderer;
		this.mailService = mailService;
		this.dispatchLogRepository = dispatchLogRepository;
		this.objectMapper = objectMapper;
		this.maxRecipientsPerRun = maxRecipientsPerRun;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public void deliverPending(int maxBroadcasts) {
		List<Long> ids = broadcastMessageRepository.findIdsPendingDelivery(PageRequest.of(0, maxBroadcasts));
		for (Long id : ids) {
			try {
				transactionTemplate.executeWithoutResult(status -> processSingleBroadcast(id));
			} catch (Exception ex) {
				log.error("[NOTIF_BCAST] broadcast id={} failed: {}", id, ex.toString(), ex);
				transactionTemplate.executeWithoutResult(status -> broadcastMessageRepository.updateTerminalStatus(id,
						"FAILED", LocalDateTime.now()));
			}
		}
	}

	private void processSingleBroadcast(long id) {
		int claimed = broadcastMessageRepository.tryClaim(id);
		if (claimed != 1) {
			return;
		}
		BroadcastMessageEntity msg = broadcastMessageRepository.findById(id).orElse(null);
		if (msg == null) {
			return;
		}
		List<String> recipients = resolveRecipients(msg);
		if (recipients.isEmpty()) {
			log.warn("[NOTIF_BCAST] id={} no recipients", id);
			broadcastMessageRepository.updateTerminalStatus(id, "FAILED", LocalDateTime.now());
			return;
		}
		int cap = Math.min(recipients.size(), maxRecipientsPerRun);
		int ok = 0;
		int fail = 0;
		for (int i = 0; i < cap; i++) {
			String email = recipients.get(i);
			if (broadcastRecipientRepository.existsByBroadcastMessageIdAndRecipientEmailIgnoreCase(id, email)) {
				continue;
			}
			BroadcastRecipientEntity line = new BroadcastRecipientEntity();
			line.setBroadcastMessageId(id);
			line.setRecipientEmail(email);
			line.setStatus("PENDING");
			broadcastRecipientRepository.save(line);

			try {
				sendOne(msg, email);
				line.setStatus("SUCCESS");
				line.setSentAt(LocalDateTime.now());
				ok++;
				appendDispatchLog(id, email, NotificationConstants.STATUS_SUCCESS, null);
			} catch (Exception ex) {
				String err = truncate(ex.getMessage(), 3900);
				line.setStatus("FAILED");
				line.setErrorDetail(err);
				fail++;
				appendDispatchLog(id, email, NotificationConstants.STATUS_FAILED, err);
			}
			broadcastRecipientRepository.save(line);
		}
		if (cap < recipients.size()) {
			log.info("[NOTIF_BCAST] id={} partial batch {}/{} — will continue next scheduler tick", id, cap,
					recipients.size());
			msg.setStatus("PENDING");
			msg.setDeliveryRequested(true);
			broadcastMessageRepository.save(msg);
			return;
		}
		String terminal = ok > 0 ? "SENT" : "FAILED";
		broadcastMessageRepository.updateTerminalStatus(id, terminal, LocalDateTime.now());
	}

	private List<String> resolveRecipients(BroadcastMessageEntity msg) {
		String t = msg.getTargetType() == null ? "" : msg.getTargetType().trim();
		switch (t) {
		case TARGET_ALL_USERS:
			return resolutionDao.findAllUserEmails();
		case TARGET_ALL_CUSTOMERS:
			return resolutionDao.findAllCustomerEmails();
		case TARGET_ROLE_BASED:
			if (msg.getTargetRoleId() == null) {
				return List.of();
			}
			return resolutionDao.findUserEmailsForRole(msg.getTargetRoleId());
		case TARGET_CUSTOM_SEGMENT:
			return parseCustomEmails(msg.getCustomSegmentJson());
		default:
			log.warn("[NOTIF_BCAST] unknown targetType={}", t);
			return List.of();
		}
	}

	private List<String> parseCustomEmails(String json) {
		if (json == null || json.isBlank()) {
			return List.of();
		}
		try {
			JsonNode root = objectMapper.readTree(json);
			Set<String> out = new LinkedHashSet<>();
			if (root.isArray()) {
				for (JsonNode n : root) {
					if (n.isTextual()) {
						addEmail(out, n.asText());
					}
				}
			} else if (root.has("emails") && root.get("emails").isArray()) {
				for (JsonNode n : root.get("emails")) {
					if (n.isTextual()) {
						addEmail(out, n.asText());
					}
				}
			}
			return new ArrayList<>(out);
		} catch (Exception e) {
			log.error("[NOTIF_BCAST] invalid CUSTOM_SEGMENT json: {}", e.toString());
			return List.of();
		}
	}

	private static void addEmail(Set<String> out, String raw) {
		if (raw == null) {
			return;
		}
		String e = raw.trim().toLowerCase();
		if (!e.isEmpty()) {
			out.add(e);
		}
	}

	private void sendOne(BroadcastMessageEntity msg, String email) {
		java.util.HashMap<String, String> vars = new java.util.HashMap<>();
		vars.put("email", email);
		vars.put("name", email);
		vars.put("date", java.time.LocalDate.now().toString());
		vars.put("promotion", "");
		String subject = renderer.render(msg.getSubject(), vars);
		String html = renderer.render(msg.getBody(), vars);
		String text = html.replaceAll("<[^>]+>", "");
		mailService.sendHtmlEmail(email, subject, html, text);
	}

	private void appendDispatchLog(long broadcastId, String email, String status, String err) {
		NotificationDispatchLogEntity row = new NotificationDispatchLogEntity();
		row.setProcessType(NotificationConstants.PROCESS_BROADCAST);
		row.setTemplateKey(NotificationConstants.TEMPLATE_BROADCAST_DEFAULT);
		row.setUserId(null);
		row.setRecipientEmail(email);
		row.setStatus(status);
		row.setErrorDetail(err);
		row.setBroadcastMessageId(broadcastId);
		dispatchLogRepository.save(row);
	}

	private static String truncate(String s, int max) {
		if (s == null) {
			return null;
		}
		return s.length() <= max ? s : s.substring(0, max);
	}
}
