package com.travel.umbridge;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "um_role", schema = "UM")
public class UmRoleEntity {

	@Id
	@Column(name = "id")
	private Long id;

	@Column(nullable = false)
	private String name;

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
