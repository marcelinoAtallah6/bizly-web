package com.settings.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.settings.common.DatabaseConstants;

/**
 * Read-only bridge view of {@code UM.UM_ROLE}. The settings module never writes to UM, it only
 * needs (name → role_type) to translate user authorities (which are strings) into stable numeric
 * role types when checking dashboard grants.
 */
@Entity
@Table(name = DatabaseConstants.UM_ROLE_TABLE, schema = DatabaseConstants.SCHEMA)
public class UmRoleRef {

	@Id
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(name = "role_type")
	private Integer roleType;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getRoleType() {
		return roleType;
	}
}
