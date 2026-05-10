package com.settings.api.dto.dashboard;

public class WidgetDetailResponse {

	private Long id;
	private String widgetType;
	private String title;
	private Long queryDefId;
	private String queryName;
	private String configJson;
	private int gridX;
	private int gridY;
	private int gridW;
	private int gridH;
	private Integer refreshSec;
	private int sortOrder;

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

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public String getConfigJson() {
		return configJson;
	}

	public void setConfigJson(String configJson) {
		this.configJson = configJson;
	}

	public int getGridX() {
		return gridX;
	}

	public void setGridX(int gridX) {
		this.gridX = gridX;
	}

	public int getGridY() {
		return gridY;
	}

	public void setGridY(int gridY) {
		this.gridY = gridY;
	}

	public int getGridW() {
		return gridW;
	}

	public void setGridW(int gridW) {
		this.gridW = gridW;
	}

	public int getGridH() {
		return gridH;
	}

	public void setGridH(int gridH) {
		this.gridH = gridH;
	}

	public Integer getRefreshSec() {
		return refreshSec;
	}

	public void setRefreshSec(Integer refreshSec) {
		this.refreshSec = refreshSec;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}
}
