package com.store.api.storeconfig.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class StoreBusinessUnitCountryId implements Serializable {
	private Long businessUnit;
	private Long country;
}
