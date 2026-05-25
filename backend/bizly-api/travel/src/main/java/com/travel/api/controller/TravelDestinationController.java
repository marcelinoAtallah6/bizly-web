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

import com.travel.api.dto.destination.TravelDestinationDtos.*;
import com.travel.api.service.destination.ITravelDestinationService;
import com.travel.common.ApiMessages;
import com.travel.common.ApiResponse;
import com.travel.common.PageResponse;
import com.travel.security.MenuPermissionAction;
import com.travel.security.RequireMenuPermission;

@RestController
@RequestMapping("/destination")
public class TravelDestinationController {

	private static final Logger log = LogManager.getLogger(TravelDestinationController.class);

	@Autowired
	private ITravelDestinationService service;

	@PostMapping("/add")
	@RequireMenuPermission(menuRoute = "/travel/destinations", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddTravelDestinationResponse>> add(
			@RequestBody @Valid AddTravelDestinationRequest request) {
		log.info("[TRAVEL_DESTINATION][ADD] name={}", request.getName());
		return ResponseEntity.ok(ApiResponse.success(service.add(request), ApiMessages.DESTINATION_ADDED));
	}

	@PostMapping("/update")
	@RequireMenuPermission(menuRoute = "/travel/destinations", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateTravelDestinationResponse>> update(
			@RequestBody @Valid UpdateTravelDestinationRequest request) {
		log.info("[TRAVEL_DESTINATION][UPDATE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.update(request), ApiMessages.DESTINATION_UPDATED));
	}

	@PostMapping("/delete")
	@RequireMenuPermission(menuRoute = "/travel/destinations", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteTravelDestinationResponse>> delete(
			@RequestBody @Valid DeleteTravelDestinationRequest request) {
		log.info("[TRAVEL_DESTINATION][DELETE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.DESTINATION_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/travel/destinations", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetTravelDestinationResponse>> get(
			@RequestBody @Valid GetTravelDestinationRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/travel/destinations", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetTravelDestinationResponse>>> gets(
			@RequestBody @Valid GetsTravelDestinationsRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}
}
