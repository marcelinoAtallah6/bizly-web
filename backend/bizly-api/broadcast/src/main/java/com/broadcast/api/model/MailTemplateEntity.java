package com.broadcast.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.broadcast.common.DatabaseConstants;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = DatabaseConstants.NOTIF_EMAIL_TEMPLATE_TABLE, schema = "UM")
@Getter
@Setter
public class MailTemplateEntity {

	@Id
	private Long id;

	/** Tenant scope. NULL = global / system template available to all businesses. */
	@Column(name = "BUSINESS_ID")
	private Long businessId;

	@Column(name = "TEMPLATE_KEY", nullable = false, length = 64)
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
	private String activeInd;
}
