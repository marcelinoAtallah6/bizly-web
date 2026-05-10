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
@Table(name = DatabaseConstants.DASH_USER_GRANT_TABLE, schema = DatabaseConstants.SCHEMA)
public class SettingsDashboardUserGrant {

	@EmbeddedId
	private UserGrantId id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "dashboard_id", nullable = false, insertable = false, updatable = false)
	private SettingsDashboard dashboard;

	@Embeddable
	public static class UserGrantId implements Serializable {

		private static final long serialVersionUID = 1L;

		@Column(name = "dashboard_id", nullable = false)
		private Long dashboardId;

		@Column(name = "username", nullable = false, length = 120)
		private String username;

		public Long getDashboardId() {
			return dashboardId;
		}

		public void setDashboardId(Long dashboardId) {
			this.dashboardId = dashboardId;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			UserGrantId that = (UserGrantId) o;
			return Objects.equals(dashboardId, that.dashboardId) && Objects.equals(username, that.username);
		}

		@Override
		public int hashCode() {
			return Objects.hash(dashboardId, username);
		}
	}

	public UserGrantId getId() {
		return id;
	}

	public void setId(UserGrantId id) {
		this.id = id;
	}

	public SettingsDashboard getDashboard() {
		return dashboard;
	}

	public void setDashboard(SettingsDashboard dashboard) {
		this.dashboard = dashboard;
	}
}
