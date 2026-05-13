package com.bm.api.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.bm.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.CUSTOMER_SALE_TABLE, schema = DatabaseConstants.SCHEMA)
public class CustomerSale {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Tenant scope. Every read/write must filter or set this column. */
	@Column(name = "business_id")
	private Long businessId;

	@Column(name = "customer_id", nullable = false)
	private Long customerId;

	@Column(name = "customer_display_name", length = 300)
	private String customerDisplayName;

	@Column(name = "total_amount", nullable = false)
	private Double totalAmount;

	@Column(nullable = false, length = 20)
	private String status;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CustomerSaleLine> lines;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public String getCustomerDisplayName() {
		return customerDisplayName;
	}

	public void setCustomerDisplayName(String customerDisplayName) {
		this.customerDisplayName = customerDisplayName;
	}

	public Double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<CustomerSaleLine> getLines() {
		return lines;
	}

	public void setLines(List<CustomerSaleLine> lines) {
		this.lines = lines;
	}

	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
}
