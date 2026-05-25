package com.um.api.dto.catalog;

import java.util.ArrayList;
import java.util.List;

public class CatalogMenuDto {

	private Long id;
	private Long applicationId;
	private Long parentId;
	private String name;
	private String route;
	private String icon;
	private Boolean isActive;
	private String allowedRoles;
	private Integer sortOrder;
	private List<CatalogMenuDto> children = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
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

	public List<CatalogMenuDto> getChildren() {
		return children;
	}

	public void setChildren(List<CatalogMenuDto> children) {
		this.children = children;
	}
}
