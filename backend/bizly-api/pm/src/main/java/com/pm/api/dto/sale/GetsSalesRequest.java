package com.pm.api.dto.sale;

import com.pm.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetsSalesRequest extends PageRequest {

	/** When set, returns sales for this customer only (newest first). */
	private Long customerId;
}
