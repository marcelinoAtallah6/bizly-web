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
 * Grants a report to a role. Keyed by {@code (report_id, role_type)}. The
 * {@code role_type} is the stable numeric id from {@code UM_ROLE.ROLE_TYPE},
 * mirroring the dashboard-role-grant pattern so role renames never break access.
 */
@Entity
@Table(name = DatabaseConstants.REPORT_ROLE_GRANT_TABLE, schema = DatabaseConstants.SCHEMA)
public class SettingsReportRoleGrant {

	@EmbeddedId
	private GrantId id;

	@Embeddable
	public static class GrantId implements Serializable {

		private static final long serialVersionUID = 1L;

		@Column(name = "report_id", nullable = false)
		private Long reportId;

		@Column(name = "role_type", nullable = false)
		private Integer roleType;

		public Long getReportId() { return reportId; }
		public void setReportId(Long reportId) { this.reportId = reportId; }
		public Integer getRoleType() { return roleType; }
		public void setRoleType(Integer roleType) { this.roleType = roleType; }

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			GrantId g = (GrantId) o;
			return Objects.equals(reportId, g.reportId) && Objects.equals(roleType, g.roleType);
		}

		@Override
		public int hashCode() { return Objects.hash(reportId, roleType); }
	}

	public GrantId getId() { return id; }
	public void setId(GrantId id) { this.id = id; }
}
