package com.pm.umbridge;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "UM_MENUS", schema = "UM")
public class UmMenuEntity {

	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "route")
	private String route;

	public Long getId() {
		return id;
	}

	public String getRoute() {
		return route;
	}
}
