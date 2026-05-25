package com.um.api.model.workflow.engine;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.um.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.NOTIF_OUTBOX_TABLE, schema = DatabaseConstants.SCHEMA)
public class NotifOutbox {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notif_outbox_seq")
	@SequenceGenerator(name = "notif_outbox_seq", sequenceName = DatabaseConstants.NOTIF_OUTBOX_SEQ, allocationSize = 1)
	private Long id;

	@Column(name = "action_code", nullable = false, length = 80)
	private String actionCode;

	@Column(name = "business_id")
	private Long businessId;

	@Column(name = "channel", nullable = false, length = 20)
	private String channel;

	@Column(name = "template_key", length = 64)
	private String templateKey;

	@Column(name = "recipient_type", nullable = false, length = 20)
	private String recipientType;

	@Column(name = "recipient_id")
	private Long recipientId;

	@Column(name = "recipient_username", length = 120)
	private String recipientUsername;

	@Column(name = "recipient_email", length = 320)
	private String recipientEmail;

	@Lob
	@Column(name = "context_json")
	private String contextJson;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Column(name = "idempotency_key", length = 200)
	private String idempotencyKey;

	@Column(name = "attempts", nullable = false)
	private Integer attempts;

	@Column(name = "last_error", length = 4000)
	private String lastError;

	@Column(name = "scheduled_at")
	private LocalDateTime scheduledAt;

	@Column(name = "processed_at")
	private LocalDateTime processedAt;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getActionCode() {
		return actionCode;
	}

	public void setActionCode(String actionCode) {
		this.actionCode = actionCode;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getTemplateKey() {
		return templateKey;
	}

	public void setTemplateKey(String templateKey) {
		this.templateKey = templateKey;
	}

	public String getRecipientType() {
		return recipientType;
	}

	public void setRecipientType(String recipientType) {
		this.recipientType = recipientType;
	}

	public Long getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(Long recipientId) {
		this.recipientId = recipientId;
	}

	public String getRecipientUsername() {
		return recipientUsername;
	}

	public void setRecipientUsername(String recipientUsername) {
		this.recipientUsername = recipientUsername;
	}

	public String getRecipientEmail() {
		return recipientEmail;
	}

	public void setRecipientEmail(String recipientEmail) {
		this.recipientEmail = recipientEmail;
	}

	public String getContextJson() {
		return contextJson;
	}

	public void setContextJson(String contextJson) {
		this.contextJson = contextJson;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public void setIdempotencyKey(String idempotencyKey) {
		this.idempotencyKey = idempotencyKey;
	}

	public Integer getAttempts() {
		return attempts;
	}

	public void setAttempts(Integer attempts) {
		this.attempts = attempts;
	}

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}

	public LocalDateTime getScheduledAt() {
		return scheduledAt;
	}

	public void setScheduledAt(LocalDateTime scheduledAt) {
		this.scheduledAt = scheduledAt;
	}

	public LocalDateTime getProcessedAt() {
		return processedAt;
	}

	public void setProcessedAt(LocalDateTime processedAt) {
		this.processedAt = processedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
