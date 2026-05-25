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

import com.travel.api.dto.payment.TravelPaymentDtos.*;
import com.travel.api.service.payment.ITravelPaymentService;
import com.travel.common.ApiMessages;
import com.travel.common.ApiResponse;
import com.travel.common.PageResponse;
import com.travel.security.MenuPermissionAction;
import com.travel.security.RequireMenuPermission;

@RestController
@RequestMapping("/payment")
public class TravelPaymentController {

	private static final Logger log = LogManager.getLogger(TravelPaymentController.class);

	@Autowired
	private ITravelPaymentService service;

	@PostMapping("/add")
	@RequireMenuPermission(menuRoute = "/travel/finance", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddTravelPaymentResponse>> add(
			@RequestBody @Valid AddTravelPaymentRequest request) {
		log.info("[TRAVEL_PAYMENT][ADD] invoiceId={}", request.getInvoiceId());
		return ResponseEntity.ok(ApiResponse.success(service.add(request), ApiMessages.PAYMENT_ADDED));
	}

	@PostMapping("/update")
	@RequireMenuPermission(menuRoute = "/travel/finance", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateTravelPaymentResponse>> update(
			@RequestBody @Valid UpdateTravelPaymentRequest request) {
		log.info("[TRAVEL_PAYMENT][UPDATE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.update(request), ApiMessages.PAYMENT_UPDATED));
	}

	@PostMapping("/delete")
	@RequireMenuPermission(menuRoute = "/travel/finance", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteTravelPaymentResponse>> delete(
			@RequestBody @Valid DeleteTravelPaymentRequest request) {
		log.info("[TRAVEL_PAYMENT][DELETE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.PAYMENT_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/travel/finance", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetTravelPaymentResponse>> get(
			@RequestBody @Valid GetTravelPaymentRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/travel/finance", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetTravelPaymentResponse>>> gets(
			@RequestBody @Valid GetsTravelPaymentsRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}
}
