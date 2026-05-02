package com.store.api.storeconfig.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "business_unit_countries", schema = "conf")
@IdClass(StoreBusinessUnitCountryId.class)
public class StoreBsnCountries {

	@Id
	@ManyToOne
	@JoinColumn(name = "business_unit_id", referencedColumnName = "id", nullable = false)
	private StoreBusinessUnit businessUnit;

	@Id
	@ManyToOne
	@JoinColumn(name = "country_id", referencedColumnName = "id", nullable = false)
	private StoreCountries country;

}
