package com.settings.api.dto.dashboard;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class DashboardSaveRequest {

	private Long id;

	@NotBlank
	@Size(max = 200)
	private String name;

	@NotBlank
	@Size(max = 120)
	private String slug;

	@Size(max = 500)
	private String description;

	private String layoutJson;

	private boolean builtin;

	@NotNull
	@Valid
	private List<WidgetDto> widgets;

	private List<String> grantRoles;

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

	public List<WidgetDto> getWidgets() {
		return widgets;
	}

	public void setWidgets(List<WidgetDto> widgets) {
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
