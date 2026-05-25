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
@Table(name = DatabaseConstants.TRAVEL_DOCUMENT_TABLE, schema = DatabaseConstants.SCHEMA)
public class TravelDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "business_id", nullable = false)
	private Long businessId;

	@Column(name = "client_id")
	private Long clientId;

	@Column(name = "booking_id")
	private Long bookingId;

	@Column(name = "doc_type", nullable = false, length = 64)
	private String docType;

	@Column(name = "file_name", length = 300)
	private String fileName;

	@Column(name = "storage_ref", length = 500)
	private String storageRef;

	@Column(name = "uploaded_at", nullable = false)
	private LocalDateTime uploadedAt;

	@Column(name = "uploaded_by", length = 200)
	private String uploadedBy;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getBusinessId() { return businessId; }
	public void setBusinessId(Long businessId) { this.businessId = businessId; }
	public Long getClientId() { return clientId; }
	public void setClientId(Long clientId) { this.clientId = clientId; }
	public Long getBookingId() { return bookingId; }
	public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
	public String getDocType() { return docType; }
	public void setDocType(String docType) { this.docType = docType; }
	public String getFileName() { return fileName; }
	public void setFileName(String fileName) { this.fileName = fileName; }
	public String getStorageRef() { return storageRef; }
	public void setStorageRef(String storageRef) { this.storageRef = storageRef; }
	public LocalDateTime getUploadedAt() { return uploadedAt; }
	public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
	public String getUploadedBy() { return uploadedBy; }
	public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
}
