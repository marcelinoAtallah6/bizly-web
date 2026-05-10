package com.auth.api.model.permission;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "UM_ROLE_MENU_PERM", schema = "UM")
public class RoleMenuPermissionEntity {

	@EmbeddedId
	private RoleMenuPermissionId id;

	@Column(name = "allow_view", nullable = false)
	private boolean allowView;

	@Column(name = "allow_add", nullable = false)
	private boolean allowAdd;

	@Column(name = "allow_edit", nullable = false)
	private boolean allowEdit;

	@Column(name = "allow_delete", nullable = false)
	private boolean allowDelete;

	public RoleMenuPermissionId getId() {
		return id;
	}

	public void setId(RoleMenuPermissionId id) {
		this.id = id;
	}

	public boolean isAllowView() {
		return allowView;
	}

	public void setAllowView(boolean allowView) {
		this.allowView = allowView;
	}

	public boolean isAllowAdd() {
		return allowAdd;
	}

	public void setAllowAdd(boolean allowAdd) {
		this.allowAdd = allowAdd;
	}

	public boolean isAllowEdit() {
		return allowEdit;
	}

	public void setAllowEdit(boolean allowEdit) {
		this.allowEdit = allowEdit;
	}

	public boolean isAllowDelete() {
		return allowDelete;
	}

	public void setAllowDelete(boolean allowDelete) {
		this.allowDelete = allowDelete;
	}
}
