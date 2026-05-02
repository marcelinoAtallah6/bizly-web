package com.store.api.storeitem.model;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
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

import com.store.api.storeconfig.model.StoreBusinessUnit;

import lombok.Data;

@Data
@Entity
@Table(name = "items_business_unit", schema = "store")
public class StoreItemBusinessUnit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "item_business_unit_id_seq")
	@SequenceGenerator(name = "item_business_unit_id_seq", sequenceName = "store.item_business_unit_id_seq", allocationSize = 1)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	@JoinColumn(name = "item_id", referencedColumnName = "id")
	private StoreItem item;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "business_unit_id", referencedColumnName = "id")
	private StoreBusinessUnit businessUnit;

	private Integer quantity;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

}
