package com.travel.api.model;

import java.time.LocalDate;
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
@Table(name = DatabaseConstants.TRAVEL_TRIP_REQUEST_TABLE, schema = DatabaseConstants.SCHEMA)
public class TravelTripRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_id", nullable = false)
	private Long businessId;

	@Column(name = "client_id", nullable = false)
	private Long clientId;

	@Column(name = "destination_id")
	private Long destinationId;

	@Column(name = "destination_name", length = 300)
	private String destinationName;

	@Column(name = "departure_date")
	private LocalDate departureDate;

	@Column(name = "return_date")
	private LocalDate returnDate;

	@Column(name = "budget_amount")
	private Double budgetAmount;

	@Column(length = 3)
	private String currency = "USD";

	@Column(name = "traveler_count")
	private Integer travelerCount;

	@Column(name = "traveler_details")
	@Lob
	private String travelerDetails;

	@Column(nullable = false, length = 32)
	private String status = "PENDING";

	@Column(name = "quoted_amount")
	private Double quotedAmount;

	@Lob
	private String notes;

	@Column(name = "assigned_to", length = 200)
	private String assignedTo;

	@Column(name = "booking_id")
	private Long bookingId;

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
	public Long getDestinationId() { return destinationId; }
	public void setDestinationId(Long destinationId) { this.destinationId = destinationId; }
	public String getDestinationName() { return destinationName; }
	public void setDestinationName(String destinationName) { this.destinationName = destinationName; }
	public LocalDate getDepartureDate() { return departureDate; }
	public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }
	public LocalDate getReturnDate() { return returnDate; }
	public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
	public Double getBudgetAmount() { return budgetAmount; }
	public void setBudgetAmount(Double budgetAmount) { this.budgetAmount = budgetAmount; }
	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }
	public Integer getTravelerCount() { return travelerCount; }
	public void setTravelerCount(Integer travelerCount) { this.travelerCount = travelerCount; }
	public String getTravelerDetails() { return travelerDetails; }
	public void setTravelerDetails(String travelerDetails) { this.travelerDetails = travelerDetails; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public Double getQuotedAmount() { return quotedAmount; }
	public void setQuotedAmount(Double quotedAmount) { this.quotedAmount = quotedAmount; }
	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }
	public String getAssignedTo() { return assignedTo; }
	public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
	public Long getBookingId() { return bookingId; }
	public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
