package com.notification.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "NOTIF_BROADCAST_MESSAGE", schema = "UM")
public class BroadcastMessageEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bc_msg_seq")
	@SequenceGenerator(name = "bc_msg_seq", sequenceName = "UM.NOTIF_BROADCAST_MESSAGE_SEQ", allocationSize = 1)
	private Long id;

	@Column(nullable = false, length = 512)
	private String subject;

	@Lob
	@Column(nullable = false)
	private String body;

	@Column(name = "TARGET_TYPE", nullable = false, length = 32)
	private String targetType;

	@Column(name = "TARGET_ROLE_ID")
	private Long targetRoleId;

	@Lob
	@Column(name = "CUSTOM_SEGMENT_JSON")
	private String customSegmentJson;

	@Column(nullable = false, length = 20)
	private String status;

	@Column(name = "DELIVERY_REQUESTED", nullable = false)
	private boolean deliveryRequested;

	@Column(name = "CREATED_AT", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "SENT_AT")
	private LocalDateTime sentAt;

	@Column(name = "CREATED_BY", length = 200)
	private String createdBy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public Long getTargetRoleId() {
		return targetRoleId;
	}

	public void setTargetRoleId(Long targetRoleId) {
		this.targetRoleId = targetRoleId;
	}

	public String getCustomSegmentJson() {
		return customSegmentJson;
	}

	public void setCustomSegmentJson(String customSegmentJson) {
		this.customSegmentJson = customSegmentJson;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isDeliveryRequested() {
		return deliveryRequested;
	}

	public void setDeliveryRequested(boolean deliveryRequested) {
		this.deliveryRequested = deliveryRequested;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getSentAt() {
		return sentAt;
	}

	public void setSentAt(LocalDateTime sentAt) {
		this.sentAt = sentAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
}
