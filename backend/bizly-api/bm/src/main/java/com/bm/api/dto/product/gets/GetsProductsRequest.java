package com.bm.api.dto.product.gets;

import com.bm.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetsProductsRequest extends PageRequest {

	/** When true, each row includes Base64 image payload (heavier). Default false. */
	private Boolean includeImages;
}
