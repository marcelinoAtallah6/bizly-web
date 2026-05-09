package com.pm.api.service;

import com.pm.api.dto.sale.CheckoutRequest;
import com.pm.api.dto.sale.CheckoutResponse;
import com.pm.api.dto.sale.GetSaleRequest;
import com.pm.api.dto.sale.GetSaleResponse;
import com.pm.api.dto.sale.GetsSalesRequest;
import com.pm.api.dto.sale.SaleSummaryResponse;
import com.pm.common.PageResponse;

public interface ISaleService {

	CheckoutResponse checkout(CheckoutRequest request);

	GetSaleResponse get(GetSaleRequest request);

	PageResponse<SaleSummaryResponse> gets(GetsSalesRequest request);
}
