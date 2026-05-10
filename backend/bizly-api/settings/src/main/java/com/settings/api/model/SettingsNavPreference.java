package com.settings.api.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.settings.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.NAV_PREF_TABLE, schema = DatabaseConstants.SCHEMA)
public class SettingsNavPreference {

	@EmbeddedId
	private NavPrefId id;

	@Column(name = "hidden_nav", nullable = false)
	private boolean hiddenNav;

	@Embeddable
	public static class NavPrefId implements Serializable {

		private static final long serialVersionUID = 1L;

		@Column(name = "username", nullable = false, length = 120)
		private String username;

		@Column(name = "dashboard_id", nullable = false)
		private Long dashboardId;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public Long getDashboardId() {
			return dashboardId;
		}

		public void setDashboardId(Long dashboardId) {
			this.dashboardId = dashboardId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			NavPrefId navPrefId = (NavPrefId) o;
			return Objects.equals(username, navPrefId.username) && Objects.equals(dashboardId, navPrefId.dashboardId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(username, dashboardId);
		}
	}

	public NavPrefId getId() {
		return id;
	}

	public void setId(NavPrefId id) {
		this.id = id;
	}

	public boolean isHiddenNav() {
		return hiddenNav;
	}

	public void setHiddenNav(boolean hiddenNav) {
		this.hiddenNav = hiddenNav;
	}
}
