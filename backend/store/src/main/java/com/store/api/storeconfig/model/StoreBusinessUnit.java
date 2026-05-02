package com.store.api.storeconfig.model;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
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

import com.store.api.storeitem.model.StoreItemBusinessUnit;

import lombok.Data;

@Data
@Entity
@Table(name = "business_units", schema = "conf")
public class StoreBusinessUnit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "s_business_units")
	@SequenceGenerator(name = "s_business_units", sequenceName = "conf.s_business_units", allocationSize = 1)
	private Long id;

	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "type_id", referencedColumnName = "id")
	private StoreBusinessType businessType;

	// Relationship with BusinessUnitCountry (Many-to-Many using the junction table)
	@OneToMany(mappedBy = "businessUnit", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<StoreBsnCountries> businessUnitCountries;

//	// Bidirectional relationship with StoreItemBusinessUnit
//	@OneToMany(mappedBy = "businessUnit", cascade = CascadeType.ALL, orphanRemoval = true)
//	private Set<StoreItemBusinessUnit> itemBusinessUnits;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

}
