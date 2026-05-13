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
 * Grants a query definition to a specific user. Same shape as {@link SettingsReportUserGrant}.
 */
@Entity
@Table(name = DatabaseConstants.QDEF_USER_GRANT_TABLE, schema = DatabaseConstants.SCHEMA)
public class SettingsQueryDefUserGrant {

	@EmbeddedId
	private UserGrantId id;

	@Embeddable
	public static class UserGrantId implements Serializable {

		private static final long serialVersionUID = 1L;

		@Column(name = "query_def_id", nullable = false)
		private Long queryDefId;

		@Column(name = "username", nullable = false, length = 120)
		private String username;

		public Long getQueryDefId() { return queryDefId; }
		public void setQueryDefId(Long queryDefId) { this.queryDefId = queryDefId; }
		public String getUsername() { return username; }
		public void setUsername(String username) { this.username = username; }

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			UserGrantId g = (UserGrantId) o;
			return Objects.equals(queryDefId, g.queryDefId) && Objects.equals(username, g.username);
		}

		@Override
		public int hashCode() { return Objects.hash(queryDefId, username); }
	}

	public UserGrantId getId() { return id; }
	public void setId(UserGrantId id) { this.id = id; }
}
