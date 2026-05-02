package com.store.api.storeitem.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreSaleDTO {
	private Long id;
	private Long itemId;
	private Long customerId;
	private Integer quantity;
	private BigDecimal totalPrice;
	private String paymentMethod;
	private LocalDateTime saleDate;
}