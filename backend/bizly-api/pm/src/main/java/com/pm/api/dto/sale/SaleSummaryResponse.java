package com.pm.api.dto.sale;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleSummaryResponse {

	private Long id;
	private Long customerId;
	private String customerDisplayName;
	private Double totalAmount;
	private String status;
	private LocalDateTime createdAt;
}
