package com.kyc.api.customers.model;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;


@Data
@Entity
@Table(name = "kyc_documents", schema = "kyc")
public class KYCDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "kyc_documents_seq")
    @SequenceGenerator(name = "kyc_documents_seq", sequenceName = "kyc.s_kyc_documents", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference
    private KYCCustomers customer;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "document_file")
    private byte[] documentFile;

    @Column(name = "status")
    private String status = "Pending";

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}