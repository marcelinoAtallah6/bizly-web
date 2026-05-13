package com.pm.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.pm.common.DatabaseConstants;

/**
 * Read-only projection of {@code bm_service_item} used by the unified sale checkout in PM. BM owns the
 * table — add / update / deactivate flows still live in the BM module. PM only needs id, name, price,
 * active flag and tenant scope to validate a service line during {@code /pm/sale/checkout}, so we keep
 * this mapping minimal: no relations, no lifecycle columns, never written from PM.
 */
@Entity
@Table(name = DatabaseConstants.BM_SERVICE_ITEM_TABLE, schema = DatabaseConstants.SCHEMA)
public class ServiceItem {

	@Id
	private Long id;

	@Column(name = "business_id")
	private Long businessId;

	@Column(nullable = false, length = 300)
	private String name;

	@Column(nullable = false)
	private Double price;

	@Column(nullable = false)
	private Boolean active;

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

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}
}
