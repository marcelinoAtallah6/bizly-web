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
@Table(name = DatabaseConstants.TRAVEL_PACKAGE_TABLE, schema = DatabaseConstants.SCHEMA)
public class TravelPackage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_id", nullable = false)
	private Long businessId;

	@Column(length = 64)
	private String code;

	@Column(nullable = false, length = 300)
	private String name;

	@Lob
	private String description;

	@Lob
	private String inclusions;

	@Lob
	private String exclusions;

	@Column(name = "itinerary_json")
	@Lob
	private String itineraryJson;

	@Column(name = "media_json")
	@Lob
	private String mediaJson;

	@Column(name = "destinations_json")
	@Lob
	private String destinationsJson;

	@Column(name = "addons_json")
	@Lob
	private String addonsJson;

	@Column(name = "max_capacity_per_day")
	private Integer maxCapacityPerDay;

	@Column(length = 200)
	private String destination;

	@Column(name = "duration_days")
	private Integer durationDays;

	@Column(name = "base_price", nullable = false)
	private Double basePrice;

	@Column(length = 3)
	private String currency = "USD";

	@Column(nullable = false)
	private Boolean active = true;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "created_by", length = 200)
	private String createdBy;

	@Column(name = "updated_by", length = 200)
	private String updatedBy;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
	public String getCode() { return code; }
	public void setCode(String code) { this.code = code; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getInclusions() { return inclusions; }
	public void setInclusions(String inclusions) { this.inclusions = inclusions; }
	public String getExclusions() { return exclusions; }
	public void setExclusions(String exclusions) { this.exclusions = exclusions; }
	public String getItineraryJson() { return itineraryJson; }
	public void setItineraryJson(String itineraryJson) { this.itineraryJson = itineraryJson; }
	public String getMediaJson() { return mediaJson; }
	public void setMediaJson(String mediaJson) { this.mediaJson = mediaJson; }
	public String getDestinationsJson() { return destinationsJson; }
	public void setDestinationsJson(String destinationsJson) { this.destinationsJson = destinationsJson; }
	public String getAddonsJson() { return addonsJson; }
	public void setAddonsJson(String addonsJson) { this.addonsJson = addonsJson; }
	public Integer getMaxCapacityPerDay() { return maxCapacityPerDay; }
	public void setMaxCapacityPerDay(Integer maxCapacityPerDay) { this.maxCapacityPerDay = maxCapacityPerDay; }
	public String getDestination() { return destination; }
	public void setDestination(String destination) { this.destination = destination; }
	public Integer getDurationDays() { return durationDays; }
	public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
	public Double getBasePrice() { return basePrice; }
	public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }
	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }
	public Boolean getActive() { return active; }
	public void setActive(Boolean active) { this.active = active; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	public String getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
