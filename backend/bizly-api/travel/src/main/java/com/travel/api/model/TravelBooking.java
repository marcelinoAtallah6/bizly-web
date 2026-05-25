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
@Table(name = DatabaseConstants.TRAVEL_BOOKING_TABLE, schema = DatabaseConstants.SCHEMA)
public class TravelBooking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_id", nullable = false)
	private Long businessId;

	@Column(name = "client_id", nullable = false)
	private Long clientId;

	@Column(name = "package_id")
	private Long packageId;

	@Column(name = "reference_no", length = 64)
	private String referenceNo;

	@Column(nullable = false, length = 32)
	private String status = "DRAFT";

	@Column(name = "departure_date")
	private LocalDate departureDate;

	@Column(name = "return_date")
	private LocalDate returnDate;

	@Column(name = "total_amount")
	private Double totalAmount;

	@Column(length = 3)
	private String currency = "USD";

	@Lob
	private String notes;

	@Column(name = "timeline_stage", length = 32)
	private String timelineStage;

	@Column(name = "approval_status", length = 32)
	private String approvalStatus;

	@Column(name = "payment_schedule_json")
	@Lob
	private String paymentScheduleJson;

	@Column(name = "requires_approval")
	private Boolean requiresApproval;

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
	public Long getClientId() { return clientId; }
	public void setClientId(Long clientId) { this.clientId = clientId; }
	public Long getPackageId() { return packageId; }
	public void setPackageId(Long packageId) { this.packageId = packageId; }
	public String getReferenceNo() { return referenceNo; }
	public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public LocalDate getDepartureDate() { return departureDate; }
	public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }
	public LocalDate getReturnDate() { return returnDate; }
	public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
	public Double getTotalAmount() { return totalAmount; }
	public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }
	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }
	public String getTimelineStage() { return timelineStage; }
	public void setTimelineStage(String timelineStage) { this.timelineStage = timelineStage; }
	public String getApprovalStatus() { return approvalStatus; }
	public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
	public String getPaymentScheduleJson() { return paymentScheduleJson; }
	public void setPaymentScheduleJson(String paymentScheduleJson) { this.paymentScheduleJson = paymentScheduleJson; }
	public Boolean getRequiresApproval() { return requiresApproval; }
	public void setRequiresApproval(Boolean requiresApproval) { this.requiresApproval = requiresApproval; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
	public String getCreatedBy() { return createdBy; }
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	public String getUpdatedBy() { return updatedBy; }
	public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
