package com.settings.api.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.settings.common.DatabaseConstants;

/**
 * Grants a report to a specific user. Keyed by {@code (report_id, username)} so the
 * grant survives role changes; the username column is the stable {@code UM_USER.USERNAME}.
 */
@Entity
@Table(name = DatabaseConstants.REPORT_USER_GRANT_TABLE, schema = DatabaseConstants.SCHEMA)
public class SettingsReportUserGrant {

	@EmbeddedId
	private UserGrantId id;

	@Embeddable
	public static class UserGrantId implements Serializable {

		private static final long serialVersionUID = 1L;

		@Column(name = "report_id", nullable = false)
		private Long reportId;

		@Column(name = "username", nullable = false, length = 120)
		private String username;

		public Long getReportId() { return reportId; }
		public void setReportId(Long reportId) { this.reportId = reportId; }
		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			UserGrantId g = (UserGrantId) o;
			return Objects.equals(reportId, g.reportId) && Objects.equals(username, g.username);
		}

		@Override
		public int hashCode() { return Objects.hash(reportId, username); }
	}

	public UserGrantId getId() { return id; }
	public void setId(UserGrantId id) { this.id = id; }
}
