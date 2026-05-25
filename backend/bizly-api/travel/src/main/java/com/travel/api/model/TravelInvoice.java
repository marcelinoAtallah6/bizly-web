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
@Table(name = DatabaseConstants.TRAVEL_INVOICE_TABLE, schema = DatabaseConstants.SCHEMA)
public class TravelInvoice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_id", nullable = false)
	private Long businessId;

	@Column(name = "booking_id", nullable = false)
	private Long bookingId;

	@Column(name = "invoice_no", length = 64)
	private String invoiceNo;

	@Column(nullable = false)
	private Double amount;

	@Column(length = 3)
	private String currency = "USD";

	@Column(nullable = false, length = 32)
	private String status = "OPEN";

	@Column(name = "issued_at", nullable = false)
	private LocalDateTime issuedAt;

	@Column(name = "due_at")
	private LocalDateTime dueAt;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
	public Long getBookingId() { return bookingId; }
	public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
	public String getInvoiceNo() { return invoiceNo; }
	public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
	public Double getAmount() { return amount; }
	public void setAmount(Double amount) { this.amount = amount; }
	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }
	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public LocalDateTime getIssuedAt() { return issuedAt; }
	public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
	public LocalDateTime getDueAt() { return dueAt; }
	public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }
}
