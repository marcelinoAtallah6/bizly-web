package com.store.api.storeconfig.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "branches", schema = "conf")
public class StoreBranch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "s_branches")
	@SequenceGenerator(name = "s_branches", sequenceName = "conf.s_branches", allocationSize = 1)
	private Long id;

	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "business_unit_id", referencedColumnName = "id")
	private StoreBusinessUnit businessUnit;

	private String location;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

}
