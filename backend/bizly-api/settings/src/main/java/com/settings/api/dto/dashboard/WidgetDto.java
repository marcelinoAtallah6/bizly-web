package com.settings.api.dto.dashboard;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class WidgetDto {

	private Long id;

	@NotBlank
	private String widgetType;

	@NotBlank
	private String title;

	private Long queryDefId;

	private String configJson;

	@NotNull
	private Integer gridX;

	@NotNull
	private Integer gridY;

	@NotNull
	private Integer gridW;

	@NotNull
	private Integer gridH;

	private Integer refreshSec;

	@NotNull
	private Integer sortOrder;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getWidgetType() {
		return widgetType;
	}

	public void setWidgetType(String widgetType) {
		this.widgetType = widgetType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getQueryDefId() {
		return queryDefId;
	}

	public void setQueryDefId(Long queryDefId) {
		this.queryDefId = queryDefId;
	}

	public String getConfigJson() {
		return configJson;
	}

	public void setConfigJson(String configJson) {
		this.configJson = configJson;
	}

	public Integer getGridX() {
		return gridX;
	}

	public void setGridX(Integer gridX) {
		this.gridX = gridX;
	}

	public Integer getGridY() {
		return gridY;
	}

	public void setGridY(Integer gridY) {
		this.gridY = gridY;
	}

	public Integer getGridW() {
		return gridW;
	}

	public void setGridW(Integer gridW) {
		this.gridW = gridW;
	}

	public Integer getGridH() {
		return gridH;
	}

	public void setGridH(Integer gridH) {
		this.gridH = gridH;
	}

	public Integer getRefreshSec() {
		return refreshSec;
	}

	public void setRefreshSec(Integer refreshSec) {
		this.refreshSec = refreshSec;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}
}
