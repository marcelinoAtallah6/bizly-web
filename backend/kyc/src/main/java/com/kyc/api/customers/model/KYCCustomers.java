package com.kyc.api.customers.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.kyc.api.customers.dto.KYCCustomersDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "customers", schema = "kyc")
@NoArgsConstructor
@AllArgsConstructor
public class KYCCustomers {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customers_seq")
	@SequenceGenerator(name = "customers_seq", sequenceName = "kyc.s_customers", allocationSize = 1)
	private Long id;

	@Column(name = "first_name", nullable = false)
	private String firstName;

	@Column(name = "last_name", nullable = false)
	private String lastName;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "phone_number", unique = true)
	private String phoneNumber;

	@Column(name = "is_verified")
	private Boolean isVerified = false;

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

	@JsonManagedReference
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<KYCDocument> documents;
	
	  public List<KYCDocument> getDocuments() {
	        return documents;
	    }

	    public void setDocuments(List<KYCDocument> documents) {
	        this.documents = documents;
	        if (documents != null) {
	            for (KYCDocument document : documents) {
	                document.setCustomer(this);
	            }
	        }
	    }

	public KYCCustomers(KYCCustomersDTO customerDTO) {
		this.firstName = customerDTO.getFirstName();
		this.lastName = customerDTO.getLastName();
		this.email = customerDTO.getEmail();
		this.phoneNumber = customerDTO.getPhoneNumber();
		this.isVerified = customerDTO.getIsVerified();
		this.createdBy = customerDTO.getCreatedBy();
		this.createdAt = customerDTO.getCreatedAt();
		this.updatedBy = customerDTO.getUpdatedBy();
		this.updatedAt = customerDTO.getUpdatedAt();
		this.documents = customerDTO.getDocuments();

	}

	public void updateFromDTO(KYCCustomersDTO customerDTO) {
		this.firstName = customerDTO.getFirstName();
		this.lastName = customerDTO.getLastName();
		this.email = customerDTO.getEmail();
		this.phoneNumber = customerDTO.getPhoneNumber();
		this.isVerified = customerDTO.getIsVerified();
		this.createdBy = customerDTO.getCreatedBy();
		this.createdAt = customerDTO.getCreatedAt();
		this.updatedBy = customerDTO.getUpdatedBy();
		this.updatedAt = customerDTO.getUpdatedAt();
		this.documents = customerDTO.getDocuments();

	}
}
