package com.settings.api.dto.dashboard;

import java.util.List;

public class DashboardDetailResponse {

	private Long id;
	private String name;
	private String slug;
	private String description;
	private String layoutJson;
	private boolean builtin;
	private List<WidgetDetailResponse> widgets;
	/** Role names granted access (aligned with SETTINGS_DASH_ROLE_GRANT). */
	private List<String> grantRoles;
	/** Usernames granted direct access (SETTINGS_DASH_USER_GRANT). */
	private List<String> grantUsernames;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLayoutJson() {
		return layoutJson;
	}

	public void setLayoutJson(String layoutJson) {
		this.layoutJson = layoutJson;
	}

	public boolean isBuiltin() {
		return builtin;
	}

	public void setBuiltin(boolean builtin) {
		this.builtin = builtin;
	}

	public List<WidgetDetailResponse> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<WidgetDetailResponse> widgets) {
		this.widgets = widgets;
	}

	public List<String> getGrantRoles() {
		return grantRoles;
	}

	public void setGrantRoles(List<String> grantRoles) {
		this.grantRoles = grantRoles;
	}

	public List<String> getGrantUsernames() {
		return grantUsernames;
	}

	public void setGrantUsernames(List<String> grantUsernames) {
		this.grantUsernames = grantUsernames;
	}
}
