package com.bm.api.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.bm.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.BM_SERVICE_ITEM_TABLE, schema = DatabaseConstants.SCHEMA)
public class ServiceItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Tenant scope. Every read/write must filter or set this column. */
	@Column(name = "business_id")
	private Long businessId;

	@Column(nullable = false, length = 300)
	private String name;

	@Lob
	private String description;

	@Column(nullable = false)
	private Double price;

	@Column(name = "duration_minutes", nullable = false)
	private Integer durationMinutes;

	@Column(nullable = false)
	private Boolean active;

	@Lob
	@Column(name = "metadata_json")
	private String metadataJson;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "created_by", length = 200)
	private String createdBy;

	@Column(name = "updated_by", length = 200)
	private String updatedBy;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Integer getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(Integer durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }

	public String getMetadataJson() {
		return metadataJson;
	}

	public void setMetadataJson(String metadataJson) {
		this.metadataJson = metadataJson;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}
}
