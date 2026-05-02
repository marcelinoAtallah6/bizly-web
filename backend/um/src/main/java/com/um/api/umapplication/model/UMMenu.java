package com.um.api.umapplication.model;

import java.time.LocalDateTime;
import java.util.List;

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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "um_menus", schema = "um")
public class UMMenu {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "um_menus_seq")
	@SequenceGenerator(name = "um_menus_seq", sequenceName = "um.s_um_menus", allocationSize = 1)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "application_id", nullable = false)
	@JsonBackReference
	private UMApplication application;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	@JsonBackReference
	private UMMenu parentMenu;

	@OneToMany(mappedBy = "parentMenu", fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<UMMenu> menus;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "route")
	private String route;

	@Column(name = "icon")
	private String icon;

	@Column(name = "is_active")
	private Boolean isActive;

	@JsonIgnore
	@Column(name = "created_at")
	private LocalDateTime createdAt;
	@JsonIgnore
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// Getters and Setters

	public UMMenu getParentMenu() {
		return parentMenu;
	}

	public void setParentMenu(UMMenu parentMenu) {
		this.parentMenu = parentMenu;
	}

	public List<UMMenu> getMenus() {
		return menus;
	}

	public void setMenus(List<UMMenu> menus) {
		this.menus = menus;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UMApplication getApplication() {
		return application;
	}

	public void setApplication(UMApplication application) {
		this.application = application;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
