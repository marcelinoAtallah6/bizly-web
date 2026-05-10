package com.auth.api.model.menu;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Read-only mapping for enriching JWT permission rows with {@code route} (UI enforcement).
 */
@Entity
@Table(name = "UM_MENUS", schema = "UM")
public class UmMenuRouteEntity {

	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "route")
	private String route;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}
}
