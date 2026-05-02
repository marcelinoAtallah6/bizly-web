package com.store.api.storeitem.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
@Entity
@Table(name = "items", schema = "store")
public class StoreItem {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "items_seq")
	@SequenceGenerator(name = "items_seq", sequenceName = "store.s_items", allocationSize = 1)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	private String description;

	@Column(name = "category_id")
	private Integer categoryId;

	// Bidirectional relationship with StoreItemBusinessUnit
	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<StoreItemBusinessUnit> itemBusinessUnits;

	private BigDecimal price;

	private Integer quantity;

	@Column(name = "created_by")
	private Long createdBy;

	@CreationTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_by")
	private Long updatedBy;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}