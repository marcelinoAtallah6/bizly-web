package com.um.api.dto.workflow.engine;

import java.util.ArrayList;
import java.util.List;

public class WorkflowTaskCatalogCategoryDto {

	private String categoryKey;
	private String categoryLabel;
	private List<WorkflowTaskCatalogItemDto> items = new ArrayList<>();

	public String getCategoryKey() {
		return categoryKey;
	}

	public void setCategoryKey(String categoryKey) {
		this.categoryKey = categoryKey;
	}

	public String getCategoryLabel() {
		return categoryLabel;
	}

	public void setCategoryLabel(String categoryLabel) {
		this.categoryLabel = categoryLabel;
	}

	public List<WorkflowTaskCatalogItemDto> getItems() {
		return items;
	}

	public void setItems(List<WorkflowTaskCatalogItemDto> items) {
		this.items = items != null ? items : new ArrayList<>();
	}
}
