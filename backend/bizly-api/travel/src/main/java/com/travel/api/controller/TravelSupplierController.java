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

import com.travel.api.dto.supplier.TravelSupplierDtos.*;
import com.travel.api.service.supplier.ITravelSupplierService;
import com.travel.common.ApiMessages;
import com.travel.common.ApiResponse;
import com.travel.common.PageResponse;
import com.travel.security.MenuPermissionAction;
import com.travel.security.RequireMenuPermission;

@RestController
@RequestMapping("/supplier")
public class TravelSupplierController {

	private static final Logger log = LogManager.getLogger(TravelSupplierController.class);

	@Autowired
	private ITravelSupplierService service;

	@PostMapping("/add")
	@RequireMenuPermission(menuRoute = "/travel/suppliers", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddTravelSupplierResponse>> add(
			@RequestBody @Valid AddTravelSupplierRequest request) {
		log.info("[TRAVEL_SUPPLIER][ADD] name={}", request.getName());
		return ResponseEntity.ok(ApiResponse.success(service.add(request), ApiMessages.SUPPLIER_ADDED));
	}

	@PostMapping("/update")
	@RequireMenuPermission(menuRoute = "/travel/suppliers", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateTravelSupplierResponse>> update(
			@RequestBody @Valid UpdateTravelSupplierRequest request) {
		log.info("[TRAVEL_SUPPLIER][UPDATE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.update(request), ApiMessages.SUPPLIER_UPDATED));
	}

	@PostMapping("/delete")
	@RequireMenuPermission(menuRoute = "/travel/suppliers", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteTravelSupplierResponse>> delete(
			@RequestBody @Valid DeleteTravelSupplierRequest request) {
		log.info("[TRAVEL_SUPPLIER][DELETE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.SUPPLIER_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/travel/suppliers", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetTravelSupplierResponse>> get(
			@RequestBody @Valid GetTravelSupplierRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/travel/suppliers", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetTravelSupplierResponse>>> gets(
			@RequestBody @Valid GetsTravelSuppliersRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}
}
