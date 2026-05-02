package com.store.api.storeitem.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
@Entity
@Table(name = "stock_logs", schema = "store")
public class StoreStockLogs {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_logs_seq")
	@SequenceGenerator(name = "stock_logs_seq", sequenceName = "store.s_stock_logs", allocationSize = 1)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "item_id")
	private StoreItem item;

	@Column(nullable = false)
	private String action;

	@Column(nullable = false)
	private Integer quantity;

	@Column(name = "performed_by")
	private Long performedBy;

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