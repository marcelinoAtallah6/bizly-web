package com.notification.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "NOTIF_BROADCAST_RCPT", schema = "UM")
public class BroadcastRecipientEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bc_rcpt_seq")
	@SequenceGenerator(name = "bc_rcpt_seq", sequenceName = "UM.NOTIF_BROADCAST_RCPT_SEQ", allocationSize = 1)
	private Long id;

	@Column(name = "BROADCAST_MESSAGE_ID", nullable = false)
	private Long broadcastMessageId;

	@Column(name = "RECIPIENT_EMAIL", nullable = false, length = 320)
	private String recipientEmail;

	@Column(nullable = false, length = 16)
	private String status;

	@Column(name = "ERROR_DETAIL", length = 4000)
	private String errorDetail;

	@Column(name = "SENT_AT")
	private LocalDateTime sentAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getBroadcastMessageId() {
		return broadcastMessageId;
	}

	public void setBroadcastMessageId(Long broadcastMessageId) {
		this.broadcastMessageId = broadcastMessageId;
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

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}
}
