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
 * Grants a query definition to a role. Same shape as {@link SettingsReportRoleGrant};
 * the Query Builder picks this up to hide queries from non-listed roles.
 */
@Entity
@Table(name = DatabaseConstants.QDEF_ROLE_GRANT_TABLE, schema = DatabaseConstants.SCHEMA)
public class SettingsQueryDefRoleGrant {

	@EmbeddedId
	private GrantId id;

	@Embeddable
	public static class GrantId implements Serializable {

		private static final long serialVersionUID = 1L;

		@Column(name = "query_def_id", nullable = false)
		private Long queryDefId;

		@Column(name = "role_type", nullable = false)
		private Integer roleType;

		public Long getQueryDefId() { return queryDefId; }
		public void setQueryDefId(Long queryDefId) { this.queryDefId = queryDefId; }
		public Integer getRoleType() { return roleType; }
		public void setRoleType(Integer roleType) { this.roleType = roleType; }

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			GrantId g = (GrantId) o;
			return Objects.equals(queryDefId, g.queryDefId) && Objects.equals(roleType, g.roleType);
		}

		@Override
		public int hashCode() { return Objects.hash(queryDefId, roleType); }
	}

	public GrantId getId() { return id; }
	public void setId(GrantId id) { this.id = id; }
}
