package com.um.api.dto.role.gets;

import com.um.common.PageRequest;

public class GetsRolesRequest extends PageRequest {

	/**
	 * When a portal admin has selected a tenant in the business switcher, list that tenant's
	 * {@code BUSINESS_TEAM} roles. When {@code null}, the grid shows global business-type templates only.
	 */
	private Long forBusinessId;
	/**
	 * When {@code true} (and {@link #forBusinessId} is unset), list global portal catalog roles:
	 * {@code ADMIN_INTERNAL} and {@code BUSINESS_TYPE_TEMPLATE} (both with {@code business_id} null).
	 */
	private Boolean globalTemplatesOnly;

	public Long getForBusinessId() {
		return forBusinessId;
	}

	public void setForBusinessId(Long forBusinessId) {
		this.forBusinessId = forBusinessId;
	}

	public Boolean getGlobalTemplatesOnly() {
		return globalTemplatesOnly;
	}

	public void setGlobalTemplatesOnly(Boolean globalTemplatesOnly) {
		this.globalTemplatesOnly = globalTemplatesOnly;
	}
}