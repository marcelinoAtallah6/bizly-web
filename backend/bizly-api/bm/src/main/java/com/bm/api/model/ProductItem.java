package com.bm.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.bm.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.PRODUCT_ITEMS_TABLE, schema = DatabaseConstants.SCHEMA)
public class ProductItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Denormalized tenant scope (also on parent Product) — lets line-level queries
	 *  index without joining the parent. */
	@Column(name = "business_id")
	private Long businessId;

	@ManyToOne
	@JoinColumn(name = "product_id")
	private Product product;

	private Integer quantity;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }

}