package com.notification.workflow;

import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class OutboundMailService {

	private static final Logger log = LogManager.getLogger(OutboundMailService.class);

	private final JavaMailSender mailSender;
	private final String fromAddress;
	private final boolean mailEnabled;

	public OutboundMailService(JavaMailSender mailSender,
			@Value("${notification.mail-from:noreply@bizly.local}") String fromAddress,
			@Value("${notification.mail.enabled:true}") boolean mailEnabled) {
		this.mailSender = mailSender;
		this.fromAddress = fromAddress;
		this.mailEnabled = mailEnabled;
	}

	public void sendHtmlEmail(String to, String subject, String htmlBody, String textBody) {
		if (!mailEnabled) {
			log.info("[NOTIF_MAIL] MOCK send (notification.mail.enabled=false) from={} to={} subject={}", fromAddress,
					to, subject);
			return;
		}
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(fromAddress);
			helper.setTo(to);
			helper.setSubject(subject);
			boolean html = htmlBody != null && !htmlBody.isBlank();
			String body = html ? htmlBody : (textBody != null ? textBody : "");
			helper.setText(body, html);
			mailSender.send(message);
			log.info("[NOTIF_MAIL] sent to={} subject={}", to, subject);
		} catch (Exception e) {
			log.error("[NOTIF_MAIL] send failed to={} subject={}: {}", to, subject, e.toString());
			throw new IllegalStateException("Mail send failed: " + e.getMessage(), e);
		}
	}
}
