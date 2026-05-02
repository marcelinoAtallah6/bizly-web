package com.kyc.api.customers.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.kyc.api.customers.model.KYCCustomers;
import com.kyc.api.customers.model.KYCDocument;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KYCCustomersDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Boolean isVerified;
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private List<KYCDocument> documents;

    public KYCCustomersDTO(KYCCustomers customer) {
        this.id = customer.getId();
        this.firstName = customer.getFirstName();
        this.lastName = customer.getLastName();
        this.email = customer.getEmail();
        this.phoneNumber = customer.getPhoneNumber();
        this.documents = customer.getDocuments();

    }
}