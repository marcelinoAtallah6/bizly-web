package com.um.api.umapplication.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "um_applications", schema = "um")
public class UMApplication {

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
	private List<UMMenu> menus;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_application_id", nullable = false)
	@JsonBackReference
	private UMGroupApplication groupApplication;
	

	public UMGroupApplication getGroupApplication() {
		return groupApplication;
	}

	public void setGroupApplication(UMGroupApplication groupApplication) {
		this.groupApplication = groupApplication;
	}

	public List<UMMenu> getMenus() {
		if (menus == null) return null;
	    return menus.stream()
	                .filter(menu -> menu.getParentMenu() == null) // Only include menus with no parent
	                .collect(Collectors.toList());	}

	public void setMenus(List<UMMenu> menus) {
		this.menus = menus;
	}

	// Getters and Setters
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
