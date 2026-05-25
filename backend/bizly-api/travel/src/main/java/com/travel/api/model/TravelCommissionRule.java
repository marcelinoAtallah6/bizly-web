package com.travel.api.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.travel.common.DatabaseConstants;

@Entity
@Table(name = DatabaseConstants.TRAVEL_COMMISSION_RULE_TABLE, schema = DatabaseConstants.SCHEMA)
public class TravelCommissionRule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_id", nullable = false)
	private Long businessId;

	@Column(nullable = false, length = 200)
	private String name;

	@Column(name = "rule_type", nullable = false, length = 32)
	private String ruleType;

	@Column(name = "rate_percent")
	private Double ratePercent;

	@Column(name = "flat_amount")
	private Double flatAmount;

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
	public String getRuleType() { return ruleType; }
	public void setRuleType(String ruleType) { this.ruleType = ruleType; }
	public Double getRatePercent() { return ratePercent; }
	public void setRatePercent(Double ratePercent) { this.ratePercent = ratePercent; }
	public Double getFlatAmount() { return flatAmount; }
	public void setFlatAmount(Double flatAmount) { this.flatAmount = flatAmount; }
	public Boolean getActive() { return active; }
	public void setActive(Boolean active) { this.active = active; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
