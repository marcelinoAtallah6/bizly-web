package com.notification.repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * Reads pending rows from {@code UM.UM_NOTIF_OUTBOX} (workflow-engine notifications).
 */
@Repository
public class NotifOutboxQueryDao {

	private final JdbcTemplate jdbcTemplate;

	public NotifOutboxQueryDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private static final RowMapper<PendingOutboxRow> ROW_MAPPER = (rs, i) -> {
		PendingOutboxRow row = new PendingOutboxRow();
		row.setId(rs.getLong("ID"));
		row.setActionCode(rs.getString("ACTION_CODE"));
		row.setBusinessId(rs.getObject("BUSINESS_ID") != null ? rs.getLong("BUSINESS_ID") : null);
		row.setChannel(rs.getString("CHANNEL"));
		row.setTemplateKey(rs.getString("TEMPLATE_KEY"));
		row.setRecipientType(rs.getString("RECIPIENT_TYPE"));
		row.setRecipientId(rs.getObject("RECIPIENT_ID") != null ? rs.getLong("RECIPIENT_ID") : null);
		row.setRecipientEmail(rs.getString("RECIPIENT_EMAIL"));
		row.setContextJson(rs.getString("CONTEXT_JSON"));
		row.setAttempts(rs.getInt("ATTEMPTS"));
		Timestamp created = rs.getTimestamp("CREATED_AT");
		if (created != null) {
			row.setCreatedAt(created.toLocalDateTime());
		}
		return row;
	};

	public List<PendingOutboxRow> findPending(int limit) {
		return jdbcTemplate.query(
				"SELECT ID, ACTION_CODE, BUSINESS_ID, CHANNEL, TEMPLATE_KEY, RECIPIENT_TYPE, RECIPIENT_ID, "
						+ "RECIPIENT_EMAIL, CONTEXT_JSON, ATTEMPTS, CREATED_AT "
						+ "FROM UM.UM_NOTIF_OUTBOX WHERE STATUS = 'PENDING' "
						+ "AND (SCHEDULED_AT IS NULL OR SCHEDULED_AT <= SYSTIMESTAMP) "
						+ "ORDER BY CREATED_AT FETCH FIRST " + limit + " ROWS ONLY",
				ROW_MAPPER);
	}

	public int markProcessing(Long id) {
		return jdbcTemplate.update(
				"UPDATE UM.UM_NOTIF_OUTBOX SET STATUS = 'PROCESSING', ATTEMPTS = ATTEMPTS + 1 WHERE ID = ? AND STATUS = 'PENDING'",
				id);
	}

	public void markSent(Long id) {
		jdbcTemplate.update(
				"UPDATE UM.UM_NOTIF_OUTBOX SET STATUS = 'SENT', PROCESSED_AT = SYSTIMESTAMP, LAST_ERROR = NULL WHERE ID = ?",
				id);
	}

	public void markFailed(Long id, String error) {
		jdbcTemplate.update(
				"UPDATE UM.UM_NOTIF_OUTBOX SET STATUS = 'FAILED', PROCESSED_AT = SYSTIMESTAMP, LAST_ERROR = ? WHERE ID = ?",
				truncate(error, 3900), id);
	}

	private static String truncate(String s, int max) {
		if (s == null) {
			return null;
		}
		return s.length() <= max ? s : s.substring(0, max);
	}

	public static final class PendingOutboxRow {
		private Long id;
		private String actionCode;
		private Long businessId;
		private String channel;
		private String templateKey;
		private String recipientType;
		private Long recipientId;
		private String recipientEmail;
		private String contextJson;
		private int attempts;
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

		public int getAttempts() {
			return attempts;
		}

		public void setAttempts(int attempts) {
			this.attempts = attempts;
		}

		public LocalDateTime getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
		}
	}
}
