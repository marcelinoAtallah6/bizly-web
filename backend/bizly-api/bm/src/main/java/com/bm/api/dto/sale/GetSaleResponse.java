package com.bm.api.dto.sale;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetSaleResponse {

	private Long id;
	private Long customerId;
	private String customerDisplayName;
	private Double totalAmount;
	private String status;
	private LocalDateTime createdAt;
	private List<SaleLineResponse> lines;
}
