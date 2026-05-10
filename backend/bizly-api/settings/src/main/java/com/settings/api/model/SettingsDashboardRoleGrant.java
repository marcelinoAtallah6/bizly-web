package com.settings.api.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.settings.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.DASH_ROLE_GRANT_TABLE, schema = DatabaseConstants.SCHEMA)
public class SettingsDashboardRoleGrant {

	@EmbeddedId
	private GrantId id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "dashboard_id", nullable = false, insertable = false, updatable = false)
	private SettingsDashboard dashboard;

	@Embeddable
	public static class GrantId implements Serializable {

		private static final long serialVersionUID = 1L;

		@Column(name = "dashboard_id", nullable = false)
		private Long dashboardId;

		@Column(name = "role_name", nullable = false, length = 120)
		private String roleName;

		public Long getDashboardId() {
			return dashboardId;
		}

		public void setDashboardId(Long dashboardId) {
			this.dashboardId = dashboardId;
		}

		public String getRoleName() {
			return roleName;
		}

		public void setRoleName(String roleName) {
			this.roleName = roleName;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			GrantId grantId = (GrantId) o;
			return Objects.equals(dashboardId, grantId.dashboardId) && Objects.equals(roleName, grantId.roleName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(dashboardId, roleName);
		}
	}

	public GrantId getId() {
		return id;
	}

	public void setId(GrantId id) {
		this.id = id;
	}

	public SettingsDashboard getDashboard() {
		return dashboard;
	}

	public void setDashboard(SettingsDashboard dashboard) {
		this.dashboard = dashboard;
	}
}
