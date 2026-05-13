package com.auth.api.model.user;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "um_role", schema = "um")
public class RoleEntity {

	@Id
	@Column(name = "id")
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(name = "role_type")
	private Integer roleType;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	/**
	 * FK to {@code UM.UM_ROLE_LEVEL}. Identifies whether this role is an
	 * ADMIN-level role (system-wide) or a BUSINESS-level role (tenant scoped).
	 * Registration only accepts roles whose level is BUSINESS-assignable.
	 */
	@Column(name = "role_level_id")
	private Long roleLevelId;

	/**
	 * 1 = this role is auto-assigned when a user finishes the
	 * {@code /auth/register-business} flow. Exactly one BUSINESS-level row
	 * should carry this flag.
	 */
	@Column(name = "is_default_for_registration", nullable = false)
	private Integer isDefaultForRegistration;

	/**
	 * 1 = the registration flow must refuse this role even if the client tries
	 * to send its id. Acts as a server-side blocklist on top of the level
	 * check so SUPER_ADMIN-style rows can never be auto-granted.
	 */
	@Column(name = "is_system_restricted", nullable = false)
	private Integer isSystemRestricted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getRoleType() {
		return roleType;
	}

	public void setRoleType(Integer roleType) {
		this.roleType = roleType;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Long getRoleLevelId() { return roleLevelId; }
	public void setRoleLevelId(Long roleLevelId) { this.roleLevelId = roleLevelId; }

	public Integer getIsDefaultForRegistration() { return isDefaultForRegistration; }
	public void setIsDefaultForRegistration(Integer v) { this.isDefaultForRegistration = v; }
	public boolean isDefaultForRegistration() { return isDefaultForRegistration != null && isDefaultForRegistration == 1; }

	public Integer getIsSystemRestricted() { return isSystemRestricted; }
	public void setIsSystemRestricted(Integer v) { this.isSystemRestricted = v; }
	public boolean isSystemRestricted() { return isSystemRestricted != null && isSystemRestricted == 1; }

}