package com.bm.api.service.sale;

import com.bm.api.dto.sale.CheckoutRequest;
import com.bm.api.dto.sale.CheckoutResponse;
import com.bm.api.dto.sale.GetSaleRequest;
import com.bm.api.dto.sale.GetSaleResponse;
import com.bm.api.dto.sale.GetsSalesRequest;
import com.bm.api.dto.sale.SaleSummaryResponse;
import com.bm.common.PageResponse;

public interface ISaleService {

	CheckoutResponse checkout(CheckoutRequest request);

	GetSaleResponse get(GetSaleRequest request);

	PageResponse<SaleSummaryResponse> gets(GetsSalesRequest request);
}
