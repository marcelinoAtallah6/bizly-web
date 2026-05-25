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
	private Integer sortOrder;
	private List<NavMenuItemResponse> menus;

	/** Present when role-based menu matrix is configured for the signed-in role. */
	private Boolean allowView;
	private Boolean allowAdd;
	private Boolean allowEdit;
	private Boolean allowDelete;

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

	public Integer getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder) {
		this.sortOrder = sortOrder;
	}

	public List<NavMenuItemResponse> getMenus() {
		return menus;
	}

	public void setMenus(List<NavMenuItemResponse> menus) {
		this.menus = menus;
	}

	public Boolean getAllowView() {
		return allowView;
	}

	public void setAllowView(Boolean allowView) {
		this.allowView = allowView;
	}

	public Boolean getAllowAdd() {
		return allowAdd;
	}

	public void setAllowAdd(Boolean allowAdd) {
		this.allowAdd = allowAdd;
	}

	public Boolean getAllowEdit() {
		return allowEdit;
	}

	public void setAllowEdit(Boolean allowEdit) {
		this.allowEdit = allowEdit;
	}

	public Boolean getAllowDelete() {
		return allowDelete;
	}

	public void setAllowDelete(Boolean allowDelete) {
		this.allowDelete = allowDelete;
	}
}
