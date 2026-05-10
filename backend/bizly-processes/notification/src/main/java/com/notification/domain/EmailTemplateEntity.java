package com.notification.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "NOTIF_EMAIL_TEMPLATE", schema = "UM")
public class EmailTemplateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notif_tmpl_seq")
	@SequenceGenerator(name = "notif_tmpl_seq", sequenceName = "UM.NOTIF_EMAIL_TEMPLATE_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "TEMPLATE_KEY", nullable = false, unique = true, length = 64)
	private String templateKey;

	@Column(name = "SUBJECT_TMPL", length = 512)
	private String subjectTemplate;

	@Lob
	@Column(name = "HTML_BODY")
	private String htmlBody;

	@Lob
	@Column(name = "TEXT_BODY")
	private String textBody;

	@Column(name = "ACTIVE_IND", nullable = false, length = 1)
	private String activeInd = "Y";

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTemplateKey() {
		return templateKey;
	}

	public void setTemplateKey(String templateKey) {
		this.templateKey = templateKey;
	}

	public String getSubjectTemplate() {
		return subjectTemplate;
	}

	public void setSubjectTemplate(String subjectTemplate) {
		this.subjectTemplate = subjectTemplate;
	}

	public String getHtmlBody() {
		return htmlBody;
	}

	public void setHtmlBody(String htmlBody) {
		this.htmlBody = htmlBody;
	}

	public String getTextBody() {
		return textBody;
	}

	public void setTextBody(String textBody) {
		this.textBody = textBody;
	}

	public String getActiveInd() {
		return activeInd;
	}

	public void setActiveInd(String activeInd) {
		this.activeInd = activeInd;
	}
}
