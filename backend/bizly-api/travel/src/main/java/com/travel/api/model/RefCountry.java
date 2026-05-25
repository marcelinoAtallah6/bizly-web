package com.travel.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.travel.common.DatabaseConstants;

@Entity
@Table(name = "ref_country", schema = DatabaseConstants.SCHEMA)
public class RefCountry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "iso2", nullable = false, length = 2)
	private String iso2;

	@Column(name = "iso3", length = 3)
	private String iso3;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(name = "dial_code", length = 16)
	private String dialCode;

	@Column(nullable = false)
	private Integer active = 1;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getIso2() { return iso2; }
	public void setIso2(String iso2) { this.iso2 = iso2; }
	public String getIso3() { return iso3; }
	public void setIso3(String iso3) { this.iso3 = iso3; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDialCode() { return dialCode; }
	public void setDialCode(String dialCode) { this.dialCode = dialCode; }
	public Integer getActive() { return active; }
	public void setActive(Integer active) { this.active = active; }
}
