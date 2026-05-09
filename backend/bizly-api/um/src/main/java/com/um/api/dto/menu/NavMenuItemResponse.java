package com.um.api.dto.menu;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NavMenuItemResponse {

	private Long id;
	private String name;
	private String description;
	private String icon;
	private Boolean isActive;
	private String route;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public List<NavMenuItemResponse> getMenus() {
		return menus;
	}

	public void setMenus(List<NavMenuItemResponse> menus) {
		this.menus = menus;
	}
}
