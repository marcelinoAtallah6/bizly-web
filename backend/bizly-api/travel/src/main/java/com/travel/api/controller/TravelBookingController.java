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

import com.travel.api.dto.booking.TravelBookingDtos.*;
import com.travel.api.service.booking.ITravelBookingService;
import com.travel.common.ApiMessages;
import com.travel.common.ApiResponse;
import com.travel.common.PageResponse;
import com.travel.security.MenuPermissionAction;
import com.travel.security.RequireMenuPermission;

@RestController
@RequestMapping("/booking")
public class TravelBookingController {

	private static final Logger log = LogManager.getLogger(TravelBookingController.class);

	@Autowired
	private ITravelBookingService service;

	@PostMapping("/add")
	@RequireMenuPermission(menuRoute = "/travel/bookings", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddTravelBookingResponse>> add(HttpServletRequest httpRequest,
			@RequestBody @Valid AddTravelBookingRequest request) {
		log.info("[TRAVEL_BOOKING][ADD] clientId={}", request.getClientId());
		return ResponseEntity.ok(ApiResponse.success(service.add(request, httpRequest.getHeader("X-User")),
				ApiMessages.BOOKING_ADDED));
	}

	@PostMapping("/update")
	@RequireMenuPermission(menuRoute = "/travel/bookings", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateTravelBookingResponse>> update(HttpServletRequest httpRequest,
			@RequestBody @Valid UpdateTravelBookingRequest request) {
		log.info("[TRAVEL_BOOKING][UPDATE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.update(request, httpRequest.getHeader("X-User")),
				ApiMessages.BOOKING_UPDATED));
	}

	@PostMapping("/delete")
	@RequireMenuPermission(menuRoute = "/travel/bookings", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteTravelBookingResponse>> delete(
			@RequestBody @Valid DeleteTravelBookingRequest request) {
		log.info("[TRAVEL_BOOKING][DELETE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.BOOKING_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/travel/bookings", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetTravelBookingResponse>> get(
			@RequestBody @Valid GetTravelBookingRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/travel/bookings", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetTravelBookingResponse>>> gets(
			@RequestBody @Valid GetsTravelBookingsRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/availability")
	@RequireMenuPermission(menuRoute = "/travel/bookings", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<BookingAvailabilityResponse>> availability(
			@RequestBody @Valid BookingAvailabilityRequest request) {
		log.info("[TRAVEL_BOOKING][AVAILABILITY] packageId={} {}-{}", request.getPackageId(), request.getYear(),
				request.getMonth());
		return ResponseEntity.ok(ApiResponse.success(service.availability(request), ApiMessages.SUCCESS));
	}
}
