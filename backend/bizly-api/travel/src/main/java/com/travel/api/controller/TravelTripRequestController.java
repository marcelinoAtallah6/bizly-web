package com.travel.api.controller;

import javax.servlet.http.HttpServletRequest;
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

import com.travel.api.dto.triprequest.TravelTripRequestDtos.*;
import com.travel.api.service.triprequest.ITravelTripRequestService;
import com.travel.common.ApiMessages;
import com.travel.common.ApiResponse;
import com.travel.common.PageResponse;
import com.travel.security.MenuPermissionAction;
import com.travel.security.RequireMenuPermission;

@RestController
@RequestMapping("/trip-request")
public class TravelTripRequestController {

	private static final Logger log = LogManager.getLogger(TravelTripRequestController.class);

	@Autowired
	private ITravelTripRequestService service;

	@PostMapping("/add")
	@RequireMenuPermission(menuRoute = "/travel/trip-requests", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddTravelTripRequestResponse>> add(
			@RequestBody @Valid AddTravelTripRequestRequest request) {
		log.info("[TRAVEL_TRIP_REQUEST][ADD] clientId={}", request.getClientId());
		return ResponseEntity.ok(ApiResponse.success(service.add(request), ApiMessages.TRIP_REQUEST_ADDED));
	}

	@PostMapping("/update")
	@RequireMenuPermission(menuRoute = "/travel/trip-requests", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateTravelTripRequestResponse>> update(
			@RequestBody @Valid UpdateTravelTripRequestRequest request) {
		log.info("[TRAVEL_TRIP_REQUEST][UPDATE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.update(request), ApiMessages.TRIP_REQUEST_UPDATED));
	}

	@PostMapping("/delete")
	@RequireMenuPermission(menuRoute = "/travel/trip-requests", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteTravelTripRequestResponse>> delete(
			@RequestBody @Valid DeleteTravelTripRequestRequest request) {
		log.info("[TRAVEL_TRIP_REQUEST][DELETE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.TRIP_REQUEST_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/travel/trip-requests", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetTravelTripRequestResponse>> get(
			@RequestBody @Valid GetTravelTripRequestRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/travel/trip-requests", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetTravelTripRequestResponse>>> gets(
			@RequestBody @Valid GetsTravelTripRequestsRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/quote")
	@RequireMenuPermission(menuRoute = "/travel/trip-requests", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<QuoteTravelTripRequestResponse>> quote(
			@RequestBody @Valid QuoteTravelTripRequestRequest request) {
		log.info("[TRAVEL_TRIP_REQUEST][QUOTE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.quote(request), ApiMessages.TRIP_REQUEST_QUOTED));
	}

	@PostMapping("/accept")
	@RequireMenuPermission(menuRoute = "/travel/trip-requests", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<AcceptTravelTripRequestResponse>> accept(
			@RequestBody @Valid AcceptTravelTripRequestRequest request) {
		log.info("[TRAVEL_TRIP_REQUEST][ACCEPT] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.accept(request), ApiMessages.TRIP_REQUEST_ACCEPTED));
	}

	@PostMapping("/reject")
	@RequireMenuPermission(menuRoute = "/travel/trip-requests", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<RejectTravelTripRequestResponse>> reject(
			@RequestBody @Valid RejectTravelTripRequestRequest request) {
		log.info("[TRAVEL_TRIP_REQUEST][REJECT] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.reject(request), ApiMessages.TRIP_REQUEST_REJECTED));
	}

	@PostMapping("/convert")
	@RequireMenuPermission(menuRoute = "/travel/trip-requests", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<ConvertTravelTripRequestResponse>> convert(
			@RequestBody @Valid ConvertTravelTripRequestRequest request, HttpServletRequest httpRequest) {
		log.info("[TRAVEL_TRIP_REQUEST][CONVERT] id={}", request.getId());
		String username = httpRequest.getHeader("X-User");
		if (username == null || username.isBlank()) {
			username = "system";
		}
		return ResponseEntity.ok(ApiResponse.success(
				service.convert(request, username), ApiMessages.TRIP_REQUEST_CONVERTED));
	}
}
