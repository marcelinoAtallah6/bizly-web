package com.um.api.dto.catalog;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

public class ReorderCatalogRequest {

	/** {@code APPLICATION} or {@code MENU}. */
	@NotBlank
	private String scope;

	/** Required when {@code scope=MENU}. */
	private Long applicationId;

	/** When {@code scope=MENU}, reorder siblings under this parent; omit for root-level menus. */
	private Long parentId;

	@NotEmpty
	private List<Long> orderedIds;

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public List<Long> getOrderedIds() {
		return orderedIds;
	}

	public void setOrderedIds(List<Long> orderedIds) {
		this.orderedIds = orderedIds;
	}
}
