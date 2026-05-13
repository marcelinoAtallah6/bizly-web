package com.broadcast.api.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.broadcast.api.enums.BroadcastStatus;
import com.broadcast.common.DatabaseConstants;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = DatabaseConstants.NOTIF_BROADCAST_MESSAGE_TABLE, schema = "UM")
@Getter
@Setter
public class BroadcastMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bc_msg_seq")
	@SequenceGenerator(name = "bc_msg_seq", sequenceName = DatabaseConstants.NOTIF_BROADCAST_MESSAGE_SEQ, allocationSize = 1)
	private Long id;

	/** Tenant scope. Every broadcast belongs to exactly one business. */
	@Column(name = "BUSINESS_ID")
	private Long businessId;

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

	@PrePersist
	public void prePersist() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (status == null) {
			status = BroadcastStatus.PENDING.name();
		}
	}
}
