package com.kyc.api.customers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KYCCustomersAdditionalInfoDTO {
    private String firstName;
    private String lastName;
}