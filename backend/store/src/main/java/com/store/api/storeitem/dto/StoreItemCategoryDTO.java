package com.store.api.storeitem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemCategoryDTO {
	private Long id;
	private String name;
	private String description;
	private Long parentId;
}