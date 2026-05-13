package com.um.api.model.role;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.um.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.ROLE_TABLE, schema = DatabaseConstants.SCHEMA)
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq")
	@SequenceGenerator(name = "role_seq", sequenceName = DatabaseConstants.ROLE_SEQ, allocationSize = 1)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(name = "role_type")
	private Integer roleType;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "role_level_id")
	private Long roleLevelId;

	@Column(name = "is_default_for_registration")
	private Integer isDefaultForRegistration;

	@Column(name = "is_system_restricted")
	private Integer isSystemRestricted;

	public Long getRoleLevelId() { return roleLevelId; }
	public void setRoleLevelId(Long roleLevelId) { this.roleLevelId = roleLevelId; }

	public Integer getIsDefaultForRegistration() { return isDefaultForRegistration; }
	public void setIsDefaultForRegistration(Integer v) { this.isDefaultForRegistration = v; }
	public boolean isDefaultForRegistration() { return isDefaultForRegistration != null && isDefaultForRegistration == 1; }

	public Integer getIsSystemRestricted() { return isSystemRestricted; }
	public void setIsSystemRestricted(Integer v) { this.isSystemRestricted = v; }
	public boolean isSystemRestricted() { return isSystemRestricted != null && isSystemRestricted == 1; }

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
}