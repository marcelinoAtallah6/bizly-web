package com.notification.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "NOTIF_DISPATCH_LOG", schema = "UM")
public class NotificationDispatchLogEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notif_log_seq")
	@SequenceGenerator(name = "notif_log_seq", sequenceName = "UM.NOTIF_DISPATCH_LOG_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "PROCESS_TYPE", nullable = false, length = 32)
	private String processType;

	@Column(name = "TEMPLATE_KEY", length = 64)
	private String templateKey;

	@Column(name = "USER_ID")
	private Long userId;

	@Column(name = "RECIPIENT_EMAIL", length = 320)
	private String recipientEmail;

	@Column(name = "STATUS", nullable = false, length = 16)
	private String status;

	@Column(name = "ERROR_DETAIL", length = 4000)
	private String errorDetail;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "BROADCAST_MESSAGE_ID")
	private Long broadcastMessageId;

	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProcessType() {
		return processType;
	}

	public void setProcessType(String processType) {
		this.processType = processType;
	}

	public String getTemplateKey() {
		return templateKey;
	}

	public void setTemplateKey(String templateKey) {
		this.templateKey = templateKey;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getRecipientEmail() {
		return recipientEmail;
	}

	public void setRecipientEmail(String recipientEmail) {
		this.recipientEmail = recipientEmail;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorDetail() {
		return errorDetail;
	}

	public void setErrorDetail(String errorDetail) {
		this.errorDetail = errorDetail;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Long getBroadcastMessageId() {
		return broadcastMessageId;
	}

	public void setBroadcastMessageId(Long broadcastMessageId) {
		this.broadcastMessageId = broadcastMessageId;
	}
}
