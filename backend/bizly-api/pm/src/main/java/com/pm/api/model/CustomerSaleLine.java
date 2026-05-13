package com.pm.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.pm.common.DatabaseConstants;

/**
 * A single line on a {@link CustomerSale}. Lines are either {@code PRODUCT} (with {@link #productId}
 * populated) or {@code SERVICE} (with {@link #serviceId} populated). The columns mirror BM's mapping
 * exactly so we can move the unified product+service checkout from {@code /bm/sale/*} to
 * {@code /pm/sale/*} without changing the underlying table.
 */
@Entity
@Table(name = DatabaseConstants.CUSTOMER_SALE_LINE_TABLE, schema = DatabaseConstants.SCHEMA)
public class CustomerSaleLine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Denormalised tenant scope (also on parent {@link CustomerSale}). */
	@Column(name = "business_id")
	private Long businessId;

	@ManyToOne(optional = false)
	@JoinColumn(name = "sale_id")
	private CustomerSale sale;

	@Column(name = "product_id")
	private Long productId;

	/** {@code PRODUCT} or {@code SERVICE}. */
	@Column(name = "line_type", nullable = false, length = 20)
	private String lineType;

	@Column(name = "service_id")
	private Long serviceId;

	@Column(name = "product_name", nullable = false, length = 200)
	private String productName;

	@Column(nullable = false)
	private Integer quantity;

	@Column(name = "unit_price", nullable = false)
	private Double unitPrice;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	public CustomerSale getSale() {
		return sale;
	}

	public void setSale(CustomerSale sale) {
		this.sale = sale;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getLineType() {
		return lineType;
	}

	public void setLineType(String lineType) {
		this.lineType = lineType;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}
}
