package com.store.api.storeitem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreStockLogsDTO {
	private Long id;
	private Long itemId;
	private String action;
	private Integer quantity;
}