package com.notification.workflow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.notification.domain.EmailTemplateEntity;
import com.notification.domain.NotificationDispatchLogEntity;
import com.notification.repository.EmailTemplateRepository;
import com.notification.repository.NotificationDispatchLogRepository;
import com.notification.repository.UmUserBirthdayQueryDao;
import com.notification.repository.UmUserWelcomeFlagDao;

/**
 * Process-oriented notification workflows (welcome, birthday). Templates load from the database;
 * dispatch outcomes are logged for auditing.
 */
@Service
public class NotificationWorkflowEngine {

	private static final Logger log = LogManager.getLogger(NotificationWorkflowEngine.class);

	private final EmailTemplateRepository templateRepository;
	private final NotificationDispatchLogRepository dispatchLogRepository;
	private final UmUserBirthdayQueryDao birthdayQueryDao;
	private final UmUserWelcomeFlagDao welcomeFlagDao;
	private final TemplatePlaceholderRenderer renderer;
	private final OutboundMailService mailService;

	private final String birthdayPromoCode;

	public NotificationWorkflowEngine(EmailTemplateRepository templateRepository,
			NotificationDispatchLogRepository dispatchLogRepository,
			UmUserBirthdayQueryDao birthdayQueryDao,
			UmUserWelcomeFlagDao welcomeFlagDao,
			TemplatePlaceholderRenderer renderer,
			OutboundMailService mailService,
			@Value("${notification.birthday-promo-code:BDAY-PROMO}") String birthdayPromoCode) {
		this.templateRepository = templateRepository;
		this.dispatchLogRepository = dispatchLogRepository;
		this.birthdayQueryDao = birthdayQueryDao;
		this.welcomeFlagDao = welcomeFlagDao;
		this.renderer = renderer;
		this.mailService = mailService;
		this.birthdayPromoCode = birthdayPromoCode;
	}

	/**
	 * Welcome workflow — scheduler picks users with {@code NOTIF_WELCOME_FLAG = 0} on {@code UM.UM_USER}.
	 */
	public void handleUserCreatedEvent(UserCreatedNotificationPayload payload) {
		log.info("[NOTIF_PROCESS] WELCOME start userId={} email={}", payload.getUserId(), payload.getEmail());
		if (payload.getEmail() == null || payload.getEmail().isBlank()) {
			log.warn("[NOTIF_PROCESS] WELCOME skipped — no email for userId={}", payload.getUserId());
			appendLog(NotificationConstants.PROCESS_WELCOME, NotificationConstants.TEMPLATE_WELCOME, payload.getUserId(),
					null, NotificationConstants.STATUS_FAILED, "Recipient email missing");
			welcomeFlagDao.updateWelcomeFlags(payload.getUserId(), NotificationConstants.WELCOME_FLAG_PENDING,
					NotificationConstants.WELCOME_STATUS_NOT_OK);
			return;
		}

		Optional<EmailTemplateEntity> tmpl = templateRepository.findByTemplateKeyAndActiveInd(
				NotificationConstants.TEMPLATE_WELCOME, NotificationConstants.ACTIVE_YES);
		if (tmpl.isEmpty()) {
			log.error("[NOTIF_PROCESS] WELCOME template missing or inactive: {}", NotificationConstants.TEMPLATE_WELCOME);
			appendLog(NotificationConstants.PROCESS_WELCOME, NotificationConstants.TEMPLATE_WELCOME, payload.getUserId(),
					payload.getEmail(), NotificationConstants.STATUS_FAILED, "Template not found or inactive");
			welcomeFlagDao.updateWelcomeFlags(payload.getUserId(), NotificationConstants.WELCOME_FLAG_PENDING,
					NotificationConstants.WELCOME_STATUS_NOT_OK);
			return;
		}

		Map<String, String> vars = new HashMap<>();
		vars.put("firstName", nullToEmpty(payload.getFirstName()));
		vars.put("lastName", nullToEmpty(payload.getLastName()));
		vars.put("username", nullToEmpty(payload.getUsername()));
		vars.put("email", payload.getEmail());

		sendTemplatedEmail(NotificationConstants.PROCESS_WELCOME, tmpl.get(), payload.getUserId(), payload.getEmail(),
				vars);
	}

	/**
	 * Birthday workflow — invoked each scheduler tick for users whose {@code DATE_OF_BIRTH} matches today (deduped via
	 * dispatch log).
	 */
	public void pollBirthdayEmails() {
		List<BirthdayCandidate> users = birthdayQueryDao.findUsersWithBirthdayToday();
		if (!users.isEmpty()) {
			log.info("[NOTIF_PROCESS] BIRTHDAY candidates count={}", users.size());
		}

		Optional<EmailTemplateEntity> tmpl = templateRepository.findByTemplateKeyAndActiveInd(
				NotificationConstants.TEMPLATE_BIRTHDAY, NotificationConstants.ACTIVE_YES);
		if (tmpl.isEmpty()) {
			log.error("[NOTIF_PROCESS] BIRTHDAY template missing or inactive: {}", NotificationConstants.TEMPLATE_BIRTHDAY);
			return;
		}

		LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
		for (BirthdayCandidate u : users) {
			if (u.getEmail() == null || u.getEmail().isBlank()) {
				continue;
			}
			if (dispatchLogRepository.existsByUserIdAndProcessTypeAndCreatedAtGreaterThanEqual(u.getUserId(),
					NotificationConstants.PROCESS_BIRTHDAY, startOfDay)) {
				log.info("[NOTIF_PROCESS] BIRTHDAY skip userId={} — already sent today", u.getUserId());
				continue;
			}

			Map<String, String> vars = new HashMap<>();
			vars.put("firstName", nullToEmpty(u.getFirstName()));
			vars.put("lastName", nullToEmpty(u.getLastName()));
			vars.put("email", u.getEmail());
			vars.put("promoCode", birthdayPromoCode);

			sendTemplatedEmail(NotificationConstants.PROCESS_BIRTHDAY, tmpl.get(), u.getUserId(), u.getEmail(), vars);
		}
	}

	private void sendTemplatedEmail(String processType, EmailTemplateEntity tmpl, Long userId, String to,
			Map<String, String> vars) {
		boolean welcome = NotificationConstants.PROCESS_WELCOME.equals(processType);
		String subject = renderer.render(tmpl.getSubjectTemplate(), vars);
		String html = renderer.render(tmpl.getHtmlBody(), vars);
		String text = renderer.render(tmpl.getTextBody(), vars);
		try {
			mailService.sendHtmlEmail(to, subject, html, text);
			appendLog(processType, tmpl.getTemplateKey(), userId, to, NotificationConstants.STATUS_SUCCESS, null);
			if (welcome) {
				welcomeFlagDao.updateWelcomeFlags(userId, NotificationConstants.WELCOME_FLAG_SENT,
						NotificationConstants.WELCOME_STATUS_OK);
			}
		} catch (Exception ex) {
			String err = truncate(ex.getMessage(), 3900);
			log.error("[NOTIF_PROCESS] {} failed userId={} to={}: {}", processType, userId, to, err);
			appendLog(processType, tmpl.getTemplateKey(), userId, to, NotificationConstants.STATUS_FAILED, err);
			if (welcome) {
				welcomeFlagDao.updateWelcomeFlags(userId, NotificationConstants.WELCOME_FLAG_PENDING,
						NotificationConstants.WELCOME_STATUS_NOT_OK);
			}
		}
	}

	private void appendLog(String processType, String templateKey, Long userId, String email, String status,
			String error) {
		NotificationDispatchLogEntity row = new NotificationDispatchLogEntity();
		row.setProcessType(processType);
		row.setTemplateKey(templateKey);
		row.setUserId(userId);
		row.setRecipientEmail(email);
		row.setStatus(status);
		row.setErrorDetail(error);
		dispatchLogRepository.save(row);
	}

	private static String nullToEmpty(String s) {
		return s != null ? s : "";
	}

	private static String truncate(String s, int max) {
		if (s == null) {
			return null;
		}
		return s.length() <= max ? s : s.substring(0, max);
	}
}
