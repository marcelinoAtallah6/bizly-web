package com.settings.api.dto.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Lightweight reference used by the sidebar to render one nav entry per
 * active report. The route is computed server-side as {@code /reporting/run/&lt;id&gt;}
 * so the front-end stays decoupled from the URL shape.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActiveReportRef {

	private Long id;
	private String code;
	private String name;
	private String description;
	private String icon;
	private Integer sortOrder;
	private String route;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getIcon() { return icon; }
	public void setIcon(String icon) { this.icon = icon; }
	public Integer getSortOrder() { return sortOrder; }
	public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
	public String getRoute() { return route; }
	public void setRoute(String route) { this.route = route; }
}
