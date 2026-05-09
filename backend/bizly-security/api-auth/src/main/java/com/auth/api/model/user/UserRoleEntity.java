package com.auth.api.model.user;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "um_user_role")
public class UserRoleEntity {

	@EmbeddedId
	private UserRoleId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("roleId")
	@JoinColumn(name = "role_id")
	private RoleEntity role;

	public UserRoleId getId() {
		return id;
	}

	public void setId(UserRoleId id) {
		this.id = id;
	}

	public RoleEntity getRole() {
		return role;
	}

	public void setRole(RoleEntity role) {
		this.role = role;
	}

}