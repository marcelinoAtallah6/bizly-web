package com.travel.api.controller;

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

import com.travel.api.dto.invoice.TravelInvoiceDtos.*;
import com.travel.api.service.invoice.ITravelInvoiceService;
import com.travel.common.ApiMessages;
import com.travel.common.ApiResponse;
import com.travel.common.PageResponse;
import com.travel.security.MenuPermissionAction;
import com.travel.security.RequireMenuPermission;

@RestController
@RequestMapping("/invoice")
public class TravelInvoiceController {

	private static final Logger log = LogManager.getLogger(TravelInvoiceController.class);

	@Autowired
	private ITravelInvoiceService service;

	@PostMapping("/add")
	@RequireMenuPermission(menuRoute = "/travel/finance", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddTravelInvoiceResponse>> add(
			@RequestBody @Valid AddTravelInvoiceRequest request) {
		log.info("[TRAVEL_INVOICE][ADD] bookingId={}", request.getBookingId());
		return ResponseEntity.ok(ApiResponse.success(service.add(request), ApiMessages.INVOICE_ADDED));
	}

	@PostMapping("/update")
	@RequireMenuPermission(menuRoute = "/travel/finance", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateTravelInvoiceResponse>> update(
			@RequestBody @Valid UpdateTravelInvoiceRequest request) {
		log.info("[TRAVEL_INVOICE][UPDATE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.update(request), ApiMessages.INVOICE_UPDATED));
	}

	@PostMapping("/delete")
	@RequireMenuPermission(menuRoute = "/travel/finance", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteTravelInvoiceResponse>> delete(
			@RequestBody @Valid DeleteTravelInvoiceRequest request) {
		log.info("[TRAVEL_INVOICE][DELETE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.INVOICE_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/travel/finance", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetTravelInvoiceResponse>> get(
			@RequestBody @Valid GetTravelInvoiceRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/travel/finance", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetTravelInvoiceResponse>>> gets(
			@RequestBody @Valid GetsTravelInvoicesRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}
}
