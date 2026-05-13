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

import com.notification.domain.NotificationTemplateEntity;
import com.notification.domain.NotificationDispatchLogEntity;
import com.notification.repository.NotificationTemplateRepository;
import com.notification.repository.NotificationDispatchLogRepository;
import com.notification.repository.KycCustomerBirthdayQueryDao;
import com.notification.repository.KycCustomerWelcomeFlagDao;
import com.notification.repository.KycCustomerWelcomeQueryDao;
import com.notification.repository.UmUserBirthdayQueryDao;
import com.notification.repository.UmUserWelcomeFlagDao;

/**
 * Process-oriented notification workflows (welcome, birthday). Templates load from the database;
 * dispatch outcomes are logged for auditing.
 */
@Service
public class NotificationWorkflowEngine {

	private static final Logger log = LogManager.getLogger(NotificationWorkflowEngine.class);

	private final NotificationTemplateRepository templateRepository;
	private final NotificationDispatchLogRepository dispatchLogRepository;
	private final UmUserBirthdayQueryDao birthdayQueryDao;
	private final UmUserWelcomeFlagDao welcomeFlagDao;
	private final KycCustomerWelcomeQueryDao kycWelcomeQueryDao;
	private final KycCustomerWelcomeFlagDao kycWelcomeFlagDao;
	private final KycCustomerBirthdayQueryDao kycBirthdayQueryDao;
	private final TemplatePlaceholderRenderer renderer;
	private final OutboundMailService mailService;

	private final String birthdayPromoCode;

	public NotificationWorkflowEngine(NotificationTemplateRepository templateRepository,
			NotificationDispatchLogRepository dispatchLogRepository,
			UmUserBirthdayQueryDao birthdayQueryDao,
			UmUserWelcomeFlagDao welcomeFlagDao,
			KycCustomerWelcomeQueryDao kycWelcomeQueryDao,
			KycCustomerWelcomeFlagDao kycWelcomeFlagDao,
			KycCustomerBirthdayQueryDao kycBirthdayQueryDao,
			TemplatePlaceholderRenderer renderer,
			OutboundMailService mailService,
			@Value("${notification.birthday-promo-code:BDAY-PROMO}") String birthdayPromoCode) {
		this.templateRepository = templateRepository;
		this.dispatchLogRepository = dispatchLogRepository;
		this.birthdayQueryDao = birthdayQueryDao;
		this.welcomeFlagDao = welcomeFlagDao;
		this.kycWelcomeQueryDao = kycWelcomeQueryDao;
		this.kycWelcomeFlagDao = kycWelcomeFlagDao;
		this.kycBirthdayQueryDao = kycBirthdayQueryDao;
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
			appendLog(NotificationConstants.PROCESS_WELCOME, NotificationConstants.TEMPLATE_WELCOME_USER,
					payload.getUserId(), null, NotificationConstants.STATUS_FAILED, "Recipient email missing");
			welcomeFlagDao.updateWelcomeFlags(payload.getUserId(), NotificationConstants.WELCOME_FLAG_PENDING,
					NotificationConstants.WELCOME_STATUS_NOT_OK);
			return;
		}

		Optional<NotificationTemplateEntity> tmpl = resolveWelcomeUserTemplate();
		if (tmpl.isEmpty()) {
			log.error("[NOTIF_PROCESS] WELCOME template missing or inactive for keys {} / {}",
					NotificationConstants.TEMPLATE_WELCOME_USER, NotificationConstants.TEMPLATE_WELCOME_LEGACY);
			appendLog(NotificationConstants.PROCESS_WELCOME, NotificationConstants.TEMPLATE_WELCOME_USER,
					payload.getUserId(), payload.getEmail(), NotificationConstants.STATUS_FAILED,
					"Template not found or inactive");
			welcomeFlagDao.updateWelcomeFlags(payload.getUserId(), NotificationConstants.WELCOME_FLAG_PENDING,
					NotificationConstants.WELCOME_STATUS_NOT_OK);
			return;
		}

		Map<String, String> vars = new HashMap<>();
		vars.put("firstName", nullToEmpty(payload.getFirstName()));
		vars.put("lastName", nullToEmpty(payload.getLastName()));
		vars.put("username", nullToEmpty(payload.getUsername()));
		vars.put("email", payload.getEmail());
		vars.put("date", LocalDate.now().toString());

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

		Optional<NotificationTemplateEntity> tmpl = resolveBirthdayUserTemplate();
		if (tmpl.isEmpty()) {
			log.error("[NOTIF_PROCESS] BIRTHDAY user template missing or inactive (keys {} / {})",
					NotificationConstants.TEMPLATE_BIRTHDAY_USER, NotificationConstants.TEMPLATE_BIRTHDAY_LEGACY);
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

	private Optional<NotificationTemplateEntity> resolveWelcomeUserTemplate() {
		Optional<NotificationTemplateEntity> t = templateRepository
				.findByTemplateKeyAndActiveInd(NotificationConstants.TEMPLATE_WELCOME_USER, NotificationConstants.ACTIVE_YES);
		if (t.isEmpty()) {
			t = templateRepository.findByTemplateKeyAndActiveInd(NotificationConstants.TEMPLATE_WELCOME_LEGACY,
					NotificationConstants.ACTIVE_YES);
		}
		return t;
	}

	private Optional<NotificationTemplateEntity> resolveBirthdayUserTemplate() {
		Optional<NotificationTemplateEntity> t = templateRepository
				.findByTemplateKeyAndActiveInd(NotificationConstants.TEMPLATE_BIRTHDAY_USER, NotificationConstants.ACTIVE_YES);
		if (t.isEmpty()) {
			t = templateRepository.findByTemplateKeyAndActiveInd(NotificationConstants.TEMPLATE_BIRTHDAY_LEGACY,
					NotificationConstants.ACTIVE_YES);
		}
		return t;
	}

	private Optional<NotificationTemplateEntity> resolveWelcomeCustomerTemplate() {
		Optional<NotificationTemplateEntity> t = templateRepository.findByTemplateKeyAndActiveInd(
				NotificationConstants.TEMPLATE_WELCOME_CUSTOMER, NotificationConstants.ACTIVE_YES);
		if (t.isEmpty()) {
			t = templateRepository.findByTemplateKeyAndActiveInd(NotificationConstants.TEMPLATE_WELCOME_LEGACY,
					NotificationConstants.ACTIVE_YES);
		}
		return t;
	}

	private Optional<NotificationTemplateEntity> resolveBirthdayCustomerTemplate() {
		Optional<NotificationTemplateEntity> t = templateRepository.findByTemplateKeyAndActiveInd(
				NotificationConstants.TEMPLATE_BIRTHDAY_CUSTOMER, NotificationConstants.ACTIVE_YES);
		if (t.isEmpty()) {
			t = templateRepository.findByTemplateKeyAndActiveInd(NotificationConstants.TEMPLATE_BIRTHDAY_LEGACY,
					NotificationConstants.ACTIVE_YES);
		}
		return t;
	}

	/**
	 * KYC customers pending welcome (same flag semantics as {@code UM_USER.NOTIF_WELCOME_FLAG}).
	 */
	public void pollPendingCustomerWelcomes(int maxPerTick) {
		for (WelcomePendingCustomer c : kycWelcomeQueryDao.findPendingWelcome(maxPerTick)) {
			handleCustomerWelcomeEvent(c);
		}
	}

	public void handleCustomerWelcomeEvent(WelcomePendingCustomer c) {
		log.info("[NOTIF_PROCESS] WELCOME_CUSTOMER start customerId={} email={}", c.getCustomerId(), c.getEmail());
		if (c.getEmail() == null || c.getEmail().isBlank()) {
			appendLog(NotificationConstants.PROCESS_WELCOME_CUSTOMER, NotificationConstants.TEMPLATE_WELCOME_CUSTOMER,
					c.getCustomerId(), null, NotificationConstants.STATUS_FAILED, "Recipient email missing");
			kycWelcomeFlagDao.updateWelcomeFlags(c.getCustomerId(), NotificationConstants.WELCOME_FLAG_PENDING,
					NotificationConstants.WELCOME_STATUS_NOT_OK);
			return;
		}
		Optional<NotificationTemplateEntity> tmpl = resolveWelcomeCustomerTemplate();
		if (tmpl.isEmpty()) {
			log.error("[NOTIF_PROCESS] WELCOME_CUSTOMER template missing");
			appendLog(NotificationConstants.PROCESS_WELCOME_CUSTOMER, NotificationConstants.TEMPLATE_WELCOME_CUSTOMER,
					c.getCustomerId(), c.getEmail(), NotificationConstants.STATUS_FAILED, "Template not found or inactive");
			kycWelcomeFlagDao.updateWelcomeFlags(c.getCustomerId(), NotificationConstants.WELCOME_FLAG_PENDING,
					NotificationConstants.WELCOME_STATUS_NOT_OK);
			return;
		}
		Map<String, String> vars = new HashMap<>();
		vars.put("firstName", nullToEmpty(c.getFirstName()));
		vars.put("lastName", nullToEmpty(c.getLastName()));
		vars.put("name", nullToEmpty(c.getDisplayName()));
		vars.put("email", c.getEmail());
		vars.put("date", LocalDate.now().toString());
		sendCustomerTemplatedEmail(NotificationConstants.PROCESS_WELCOME_CUSTOMER, tmpl.get(), c.getCustomerId(),
				c.getEmail(), vars);
	}

	public void pollCustomerBirthdayEmails() {
		Optional<NotificationTemplateEntity> tmpl = resolveBirthdayCustomerTemplate();
		if (tmpl.isEmpty()) {
			log.error("[NOTIF_PROCESS] BIRTHDAY_CUSTOMER template missing or inactive");
			return;
		}
		LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
		for (CustomerBirthdayCandidate c : kycBirthdayQueryDao.findCustomersWithBirthdayToday()) {
			if (c.getEmail() == null || c.getEmail().isBlank()) {
				continue;
			}
			if (dispatchLogRepository.existsByUserIdAndProcessTypeAndCreatedAtGreaterThanEqual(c.getCustomerId(),
					NotificationConstants.PROCESS_BIRTHDAY_CUSTOMER, startOfDay)) {
				continue;
			}
			Map<String, String> vars = new HashMap<>();
			vars.put("firstName", nullToEmpty(c.getFirstName()));
			vars.put("lastName", nullToEmpty(c.getLastName()));
			vars.put("name", nullToEmpty(c.getDisplayName()));
			vars.put("email", c.getEmail());
			vars.put("promoCode", birthdayPromoCode);
			vars.put("promotion", birthdayPromoCode);
			vars.put("date", LocalDate.now().toString());
			sendCustomerTemplatedEmail(NotificationConstants.PROCESS_BIRTHDAY_CUSTOMER, tmpl.get(), c.getCustomerId(),
					c.getEmail(), vars);
		}
	}

	private void sendCustomerTemplatedEmail(String processType, NotificationTemplateEntity tmpl, Long customerId,
			String to, Map<String, String> vars) {
		boolean welcome = NotificationConstants.PROCESS_WELCOME_CUSTOMER.equals(processType);
		String subject = renderer.render(tmpl.getSubjectTemplate(), vars);
		String html = renderer.render(tmpl.getHtmlBody(), vars);
		String text = renderer.render(tmpl.getTextBody(), vars);
		try {
			mailService.sendHtmlEmail(to, subject, html, text);
			appendLog(processType, tmpl.getTemplateKey(), customerId, to, NotificationConstants.STATUS_SUCCESS, null);
			if (welcome) {
				kycWelcomeFlagDao.updateWelcomeFlags(customerId, NotificationConstants.WELCOME_FLAG_SENT,
						NotificationConstants.WELCOME_STATUS_OK);
			}
		} catch (Exception ex) {
			String err = truncate(ex.getMessage(), 3900);
			log.error("[NOTIF_PROCESS] {} failed customerId={} to={}: {}", processType, customerId, to, err);
			appendLog(processType, tmpl.getTemplateKey(), customerId, to, NotificationConstants.STATUS_FAILED, err);
			if (welcome) {
				kycWelcomeFlagDao.updateWelcomeFlags(customerId, NotificationConstants.WELCOME_FLAG_PENDING,
						NotificationConstants.WELCOME_STATUS_NOT_OK);
			}
		}
	}

	private void sendTemplatedEmail(String processType, NotificationTemplateEntity tmpl, Long userId, String to,
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
