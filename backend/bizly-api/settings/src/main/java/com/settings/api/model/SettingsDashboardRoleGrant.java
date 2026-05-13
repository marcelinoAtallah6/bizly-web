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

/**
 * Grants a dashboard to a role. Keyed by {@code (dashboard_id, role_type)} where {@code role_type}
 * is the stable numeric identifier from {@link com.um.api.model.role.Role#getRoleType()}. Storing
 * the type (not the role name) makes grants immune to {@code UM_ROLE.NAME} renames.
 */
@Entity
@Table(name = DatabaseConstants.DASH_ROLE_GRANT_TABLE, schema = DatabaseConstants.SCHEMA)
public class SettingsDashboardRoleGrant {

	@EmbeddedId
	private GrantId id;

	/**
	 * Denormalized mirror of {@link UmRoleRef#getName()} at save time. Some Oracle databases still
	 * have a legacy NOT NULL {@code ROLE_NAME} column; Hibernate must populate it even though
	 * {@link GrantId#roleType} is the real key. Schemas that dropped this column should re-add it
	 * nullable via {@code patch-settings-dash-role-grant-role-name-nullable-oracle.sql}.
	 */
	@Column(name = "role_name", length = 200)
	private String roleName;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "dashboard_id", nullable = false, insertable = false, updatable = false)
	private SettingsDashboard dashboard;

	@Embeddable
	public static class GrantId implements Serializable {

		private static final long serialVersionUID = 2L;

		@Column(name = "dashboard_id", nullable = false)
		private Long dashboardId;

		@Column(name = "role_type", nullable = false)
		private Integer roleType;

		public Long getDashboardId() {
			return dashboardId;
		}

		public void setDashboardId(Long dashboardId) {
			this.dashboardId = dashboardId;
		}

		public Integer getRoleType() {
			return roleType;
		}

		public void setRoleType(Integer roleType) {
			this.roleType = roleType;
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
			return Objects.equals(dashboardId, grantId.dashboardId) && Objects.equals(roleType, grantId.roleType);
		}

		@Override
		public int hashCode() {
			return Objects.hash(dashboardId, roleType);
		}
	}

	public GrantId getId() {
		return id;
	}

	public void setId(GrantId id) {
		this.id = id;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public SettingsDashboard getDashboard() {
		return dashboard;
	}

	public void setDashboard(SettingsDashboard dashboard) {
		this.dashboard = dashboard;
	}
}
