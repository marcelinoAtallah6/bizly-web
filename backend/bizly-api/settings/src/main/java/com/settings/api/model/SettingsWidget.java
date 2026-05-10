package com.settings.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.settings.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.WIDGET_TABLE, schema = DatabaseConstants.SCHEMA)
public class SettingsWidget {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "dashboard_id", nullable = false)
	private SettingsDashboard dashboard;

	@Column(name = "widget_type", nullable = false, length = 40)
	private String widgetType;

	@Column(nullable = false, length = 300)
	private String title;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "query_def_id")
	private SettingsQueryDef queryDef;

	@Lob
	@Column(name = "config_json")
	private String configJson;

	@Column(name = "grid_x", nullable = false)
	private int gridX;

	@Column(name = "grid_y", nullable = false)
	private int gridY;

	@Column(name = "grid_w", nullable = false)
	private int gridW;

	@Column(name = "grid_h", nullable = false)
	private int gridH;

	@Column(name = "refresh_sec")
	private Integer refreshSec;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SettingsDashboard getDashboard() {
		return dashboard;
	}

	public void setDashboard(SettingsDashboard dashboard) {
		this.dashboard = dashboard;
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

	public SettingsQueryDef getQueryDef() {
		return queryDef;
	}

	public void setQueryDef(SettingsQueryDef queryDef) {
		this.queryDef = queryDef;
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
