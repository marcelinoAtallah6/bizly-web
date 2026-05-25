package com.travel.api.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.travel.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.TRAVEL_SUPPLIER_TABLE, schema = DatabaseConstants.SCHEMA)
public class TravelSupplier {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_id", nullable = false)
	private Long businessId;

	@Column(nullable = false, length = 300)
	private String name;

	@Column(name = "supplier_type", length = 64)
	private String supplierType;

	@Column(name = "contact_email", length = 320)
	private String contactEmail;

	@Column(name = "contact_phone", length = 40)
	private String contactPhone;

	@Column(name = "contracts_json")
	@Lob
	private String contractsJson;

	@Column(name = "commission_notes")
	@Lob
	private String commissionNotes;

	@Column(name = "rate_table_notes")
	@Lob
	private String rateTableNotes;

	@Column(nullable = false)
	private Boolean active = true;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getSupplierType() { return supplierType; }
	public void setSupplierType(String supplierType) { this.supplierType = supplierType; }
	public String getContactEmail() { return contactEmail; }
	public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
	public String getContactPhone() { return contactPhone; }
	public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
	public String getContractsJson() { return contractsJson; }
	public void setContractsJson(String contractsJson) { this.contractsJson = contractsJson; }
	public String getCommissionNotes() { return commissionNotes; }
	public void setCommissionNotes(String commissionNotes) { this.commissionNotes = commissionNotes; }
	public String getRateTableNotes() { return rateTableNotes; }
	public void setRateTableNotes(String rateTableNotes) { this.rateTableNotes = rateTableNotes; }
	public Boolean getActive() { return active; }
	public void setActive(Boolean active) { this.active = active; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
