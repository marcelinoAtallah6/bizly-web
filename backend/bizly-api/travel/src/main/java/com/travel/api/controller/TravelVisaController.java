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

import com.travel.api.dto.visa.TravelVisaDtos.*;
import com.travel.api.service.visa.ITravelVisaApplicationService;
import com.travel.common.ApiMessages;
import com.travel.common.ApiResponse;
import com.travel.common.PageResponse;
import com.travel.security.MenuPermissionAction;
import com.travel.security.RequireMenuPermission;

@RestController
@RequestMapping("/visa")
public class TravelVisaController {

	private static final Logger log = LogManager.getLogger(TravelVisaController.class);

	@Autowired
	private ITravelVisaApplicationService service;

	@PostMapping("/add")
	@RequireMenuPermission(menuRoute = "/travel/visas", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddTravelVisaResponse>> add(
			@RequestBody @Valid AddTravelVisaRequest request) {
		log.info("[TRAVEL_VISA][ADD] clientId={}", request.getClientId());
		return ResponseEntity.ok(ApiResponse.success(service.add(request), ApiMessages.VISA_ADDED));
	}

	@PostMapping("/update")
	@RequireMenuPermission(menuRoute = "/travel/visas", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateTravelVisaResponse>> update(
			@RequestBody @Valid UpdateTravelVisaRequest request) {
		log.info("[TRAVEL_VISA][UPDATE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.update(request), ApiMessages.VISA_UPDATED));
	}

	@PostMapping("/delete")
	@RequireMenuPermission(menuRoute = "/travel/visas", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteTravelVisaResponse>> delete(
			@RequestBody @Valid DeleteTravelVisaRequest request) {
		log.info("[TRAVEL_VISA][DELETE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.VISA_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/travel/visas", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetTravelVisaResponse>> get(
			@RequestBody @Valid GetTravelVisaRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/travel/visas", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetTravelVisaResponse>>> gets(
			@RequestBody @Valid GetsTravelVisasRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}
}
