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
@Table(name = DatabaseConstants.TRAVEL_PAYMENT_TABLE, schema = DatabaseConstants.SCHEMA)
public class TravelPayment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_id", nullable = false)
	private Long businessId;

	@Column(name = "invoice_id", nullable = false)
	private Long invoiceId;

	@Column(nullable = false)
	private Double amount;

	@Column(name = "payment_method", length = 32)
	private String paymentMethod;

	@Column(name = "paid_at", nullable = false)
	private LocalDateTime paidAt;

	@Column(name = "reference_no", length = 128)
	private String referenceNo;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
	public Long getInvoiceId() { return invoiceId; }
	public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
	public Double getAmount() { return amount; }
	public void setAmount(Double amount) { this.amount = amount; }
	public String getPaymentMethod() { return paymentMethod; }
	public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
	public LocalDateTime getPaidAt() { return paidAt; }
	public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
	public String getReferenceNo() { return referenceNo; }
	public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
}
