package com.auth.api.model.permission;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class RoleMenuPermissionId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "role_id", nullable = false)
	private Long roleId;

	@Column(name = "menu_id", nullable = false)
	private Long menuId;

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public Long getMenuId() {
		return menuId;
	}

	public void setMenuId(Long menuId) {
		this.menuId = menuId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RoleMenuPermissionId that = (RoleMenuPermissionId) o;
		return Objects.equals(roleId, that.roleId) && Objects.equals(menuId, that.menuId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(roleId, menuId);
	}
}
