package com.um.api.dto.catalog;

import java.util.ArrayList;
import java.util.List;

public class CatalogApplicationDto {

	private Long id;
	private String name;
	private String description;
	private String icon;
	private String route;
	private Boolean isActive;
	private String allowedRoles;
	private Integer sortOrder;
	private List<CatalogMenuDto> menus = new ArrayList<>();

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

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getAllowedRoles() {
		return allowedRoles;
	}

	public void setAllowedRoles(String allowedRoles) {
		this.allowedRoles = allowedRoles;
	}

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	public List<CatalogMenuDto> getMenus() {
		return menus;
	}

	public void setMenus(List<CatalogMenuDto> menus) {
		this.menus = menus;
	}
}
