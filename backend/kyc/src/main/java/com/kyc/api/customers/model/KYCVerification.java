package com.kyc.api.customers.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Data
@Entity
@Table(name = "verification_logs", schema = "kyc")
public class KYCVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "verification_logs_seq")
    @SequenceGenerator(name = "verification_logs_seq", sequenceName = "kyc.s_verification_logs", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private KYCCustomers customer;

    @Column(nullable = false)
    private String action;

    @Column(name = "performed_by")
    private Long performedBy;

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
