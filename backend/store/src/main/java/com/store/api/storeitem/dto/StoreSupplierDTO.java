package com.store.api.storeitem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreSupplierDTO {
	private Long id;
	private String name;
	private String contactNumber;
	private String email;
	private String address;
}