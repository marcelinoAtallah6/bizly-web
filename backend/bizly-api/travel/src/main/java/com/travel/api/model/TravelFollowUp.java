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
@Table(name = DatabaseConstants.TRAVEL_FOLLOW_UP_TABLE, schema = DatabaseConstants.SCHEMA)
public class TravelFollowUp {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_id", nullable = false)
	private Long businessId;

	@Column(name = "client_id")
	private Long clientId;

	@Column(name = "booking_id")
	private Long bookingId;

	@Column(nullable = false, length = 300)
	private String subject;

	@Column(name = "due_at")
	private LocalDateTime dueAt;

	@Column(nullable = false, length = 32)
	private String status = "OPEN";

	@Lob
	private String notes;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "assigned_to", length = 200)
	private String assignedTo;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
	public Long getClientId() { return clientId; }
	public void setClientId(Long clientId) { this.clientId = clientId; }
	public Long getBookingId() { return bookingId; }
	public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
	public String getSubject() { return subject; }
	public void setSubject(String subject) { this.subject = subject; }
	public LocalDateTime getDueAt() { return dueAt; }
	public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public String getAssignedTo() { return assignedTo; }
	public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
}
