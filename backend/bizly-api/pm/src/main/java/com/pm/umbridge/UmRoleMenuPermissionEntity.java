package com.pm.umbridge;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "UM_ROLE_MENU_PERM", schema = "UM")
public class UmRoleMenuPermissionEntity {

	@EmbeddedId
	private UmRoleMenuPermissionId id;

	@Column(name = "allow_view", nullable = false)
	private boolean allowView;

	@Column(name = "allow_add", nullable = false)
	private boolean allowAdd;

	@Column(name = "allow_edit", nullable = false)
	private boolean allowEdit;

	@Column(name = "allow_delete", nullable = false)
	private boolean allowDelete;

	public UmRoleMenuPermissionId getId() {
		return id;
	}

	public void setId(UmRoleMenuPermissionId id) {
		this.id = id;
	}

	public boolean isAllowView() {
		return allowView;
	}

	public boolean isAllowAdd() {
		return allowAdd;
	}

	public boolean isAllowEdit() {
		return allowEdit;
	}

	public boolean isAllowDelete() {
		return allowDelete;
	}
}

