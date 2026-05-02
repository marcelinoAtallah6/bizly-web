package com.store.api.storeitem.dto;

import java.math.BigDecimal;

import com.store.api.storeitem.model.StoreItem;
import com.store.api.storeitem.model.StoreItemCategory;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StoreItemDTO {
	private Long id;
	private String name;
	private String description;
	private StoreItemCategory category;
	private Integer categoryId;
	private Long businessUnitId;
	private BigDecimal price;
	private Integer quantity;
	private Integer bsnQuantity;

	public StoreItemDTO(StoreItem item) {
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.categoryId = item.getCategoryId();
        this.price = item.getPrice();
        this.quantity = item.getQuantity();

    }
}