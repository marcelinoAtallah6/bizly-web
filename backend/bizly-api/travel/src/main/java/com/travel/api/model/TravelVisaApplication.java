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
@Table(name = DatabaseConstants.TRAVEL_VISA_APPLICATION_TABLE, schema = DatabaseConstants.SCHEMA)
public class TravelVisaApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_id", nullable = false)
	private Long businessId;

	@Column(name = "client_id", nullable = false)
	private Long clientId;

	@Column(name = "booking_id")
	private Long bookingId;

	@Column(nullable = false, length = 64)
	private String country;

	@Column(name = "visa_type", length = 64)
	private String visaType;

	@Column(nullable = false, length = 32)
	private String status = "PENDING";

	@Column(name = "submitted_at")
	private LocalDateTime submittedAt;

	@Column(name = "decision_at")
	private LocalDateTime decisionAt;

	@Lob
	private String notes;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
	public Long getClientId() { return clientId; }
	public void setClientId(Long clientId) { this.clientId = clientId; }
	public Long getBookingId() { return bookingId; }
	public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
	public String getCountry() { return country; }
	public void setCountry(String country) { this.country = country; }
	public String getVisaType() { return visaType; }
	public void setVisaType(String visaType) { this.visaType = visaType; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public LocalDateTime getSubmittedAt() { return submittedAt; }
	public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
	public LocalDateTime getDecisionAt() { return decisionAt; }
	public void setDecisionAt(LocalDateTime decisionAt) { this.decisionAt = decisionAt; }
	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
