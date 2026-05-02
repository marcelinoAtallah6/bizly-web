package com.store.api.storeconfig.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "countries", schema = "conf")
public class StoreCountries {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "s_countries")
	@SequenceGenerator(name = "s_countries", sequenceName = "conf.s_countries", allocationSize = 1)
	private Long id;

	private String name;
	private String code;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

}
