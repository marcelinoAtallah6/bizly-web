package com.um.api.dto.menu;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NavGroupItemResponse {

	private Long id;
	private String name;
	private String icon;
	private String route;
	private String description;
	private List<NavMenuItemResponse> menus;

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

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<NavMenuItemResponse> getMenus() {
		return menus;
	}

	public void setMenus(List<NavMenuItemResponse> menus) {
		this.menus = menus;
	}
}
