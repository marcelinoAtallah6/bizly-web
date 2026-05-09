package com.um.api.model.menu;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.um.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.APPLICATIONS_TABLE)
public class UmApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "um_applications_seq")
	@SequenceGenerator(name = "um_applications_seq", sequenceName = "um.s_um_applications", allocationSize = 1)
	@Column(name = "id")
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "description")
	private String description;

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	@Column(name = "icon")
	private String icon;

	@Column(name = "route")
	private String route;

	@Column(name = "is_active")
	private Boolean isActive;

	@JsonManagedReference
	@OneToMany(mappedBy = "application")
	private List<UmMenu> menus;

	public List<UmMenu> getMenus() {
		if (menus == null)
			return null;
		return menus.stream().filter(menu -> menu.getParentMenu() == null).collect(Collectors.toList());
	}

	public void setMenus(List<UmMenu> menus) {
		this.menus = menus;
	}

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
}
