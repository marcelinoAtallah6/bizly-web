package com.pm.api.controller.pm;

import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.pm.audit.Audited;
import com.pm.api.dto.sale.CheckoutRequest;
import com.pm.api.dto.sale.CheckoutResponse;
import com.pm.api.dto.sale.GetSaleRequest;
import com.pm.api.dto.sale.GetSaleResponse;
import com.pm.api.dto.sale.GetsSalesRequest;
import com.pm.api.dto.sale.SaleSummaryResponse;
import com.pm.api.service.ISaleService;
import com.pm.security.MenuPermissionAction;
import com.pm.security.RequireMenuPermission;
import com.pm.common.ApiMessages;
import com.pm.common.ApiResponse;
import com.pm.common.PageResponse;

@RestController
@RequestMapping("/sale")
public class SaleController {

	private static final Logger log = LogManager.getLogger(SaleController.class);

	@Autowired
	private ISaleService saleService;

	@PostMapping("/checkout")
	@Audited(action = "PM_SALE_CHECKOUT", resourceType = "SALE")
	@RequireMenuPermission(menuRoute = "/pm/products", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
			@RequestBody @Valid CheckoutRequest request) {
		log.info("[PM_SALE][CHECKOUT] customerId={} lines={}", request.getCustomerId(),
				request.getLines() != null ? request.getLines().size() : 0);
		return ResponseEntity.ok(ApiResponse.success(saleService.checkout(request), ApiMessages.SALE_COMPLETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/pm/products", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetSaleResponse>> get(@RequestBody @Valid GetSaleRequest request) {
		log.info("[PM_SALE][GET] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(saleService.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/pm/products", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<SaleSummaryResponse>>> gets(
			@RequestBody @Valid GetsSalesRequest request) {
		log.info("[PM_SALE][GETS] page={}", request.getPageNumber());
		return ResponseEntity.ok(ApiResponse.success(saleService.gets(request), ApiMessages.SUCCESS));
	}
}
