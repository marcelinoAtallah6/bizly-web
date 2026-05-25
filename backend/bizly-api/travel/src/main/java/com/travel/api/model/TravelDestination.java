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
@Table(name = DatabaseConstants.TRAVEL_DESTINATION_TABLE, schema = DatabaseConstants.SCHEMA)
public class TravelDestination {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_id", nullable = false)
	private Long businessId;

	@Column(nullable = false, length = 300)
	private String name;

	@Column(length = 128)
	private String country;

	@Column(length = 128)
	private String region;

	@Column(name = "visa_requirements")
	@Lob
	private String visaRequirements;

	@Column(name = "health_requirements")
	@Lob
	private String healthRequirements;

	@Column(name = "high_season_notes")
	@Lob
	private String highSeasonNotes;

	@Column(name = "low_season_notes")
	@Lob
	private String lowSeasonNotes;

	@Column(name = "travel_warnings")
	@Lob
	private String travelWarnings;

	@Column(name = "risk_level", length = 32)
	private String riskLevel;

	@Column(name = "travel_advisory")
	@Lob
	private String travelAdvisory;

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
	public String getCountry() { return country; }
	public void setCountry(String country) { this.country = country; }
	public String getRegion() { return region; }
	public void setRegion(String region) { this.region = region; }
	public String getVisaRequirements() { return visaRequirements; }
	public void setVisaRequirements(String visaRequirements) { this.visaRequirements = visaRequirements; }
	public String getHealthRequirements() { return healthRequirements; }
	public void setHealthRequirements(String healthRequirements) { this.healthRequirements = healthRequirements; }
	public String getHighSeasonNotes() { return highSeasonNotes; }
	public void setHighSeasonNotes(String highSeasonNotes) { this.highSeasonNotes = highSeasonNotes; }
	public String getLowSeasonNotes() { return lowSeasonNotes; }
	public void setLowSeasonNotes(String lowSeasonNotes) { this.lowSeasonNotes = lowSeasonNotes; }
	public String getTravelWarnings() { return travelWarnings; }
	public void setTravelWarnings(String travelWarnings) { this.travelWarnings = travelWarnings; }
	public String getRiskLevel() { return riskLevel; }
	public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
	public String getTravelAdvisory() { return travelAdvisory; }
	public void setTravelAdvisory(String travelAdvisory) { this.travelAdvisory = travelAdvisory; }
	public Boolean getActive() { return active; }
	public void setActive(Boolean active) { this.active = active; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
