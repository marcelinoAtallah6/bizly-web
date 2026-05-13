package com.pm.api.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.pm.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.PRODUCT_TABLE, schema = DatabaseConstants.SCHEMA)
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private Double price;

	private LocalDateTime createdAt;

	@Column(name = "product_image_mime", length = 64)
	private String productImageMime;

	@Lob
	@Column(name = "product_image_data")
	private byte[] productImageData;

	@Column(name = "stock_quantity")
	private Integer stockQuantity;

	/**
	 * Tenant scope. Every read/write of this entity must filter or set this column.
	 * The repository's tenant-scoped finders enforce that automatically.
	 */
	@Column(name = "business_id")
	private Long businessId;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
	private List<ProductItem> productItems;

	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<ProductItem> getProductItems() {
		return productItems;
	}

	public void setProductItems(List<ProductItem> productItems) {
		this.productItems = productItems;
	}

	public String getProductImageMime() {
		return productImageMime;
	}

	public void setProductImageMime(String productImageMime) {
		this.productImageMime = productImageMime;
	}

	public byte[] getProductImageData() {
		return productImageData;
	}

	public void setProductImageData(byte[] productImageData) {
		this.productImageData = productImageData;
	}

	public Integer getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(Integer stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

}