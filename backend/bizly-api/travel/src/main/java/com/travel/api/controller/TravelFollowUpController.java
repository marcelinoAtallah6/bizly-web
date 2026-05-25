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

import com.travel.api.dto.followup.TravelFollowUpDtos.*;
import com.travel.api.service.followup.ITravelFollowUpService;
import com.travel.common.ApiMessages;
import com.travel.common.ApiResponse;
import com.travel.common.PageResponse;
import com.travel.security.MenuPermissionAction;
import com.travel.security.RequireMenuPermission;

@RestController
@RequestMapping("/follow-up")
public class TravelFollowUpController {

	private static final Logger log = LogManager.getLogger(TravelFollowUpController.class);

	@Autowired
	private ITravelFollowUpService service;

	@PostMapping("/add")
	@RequireMenuPermission(menuRoute = "/travel/follow-ups", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddTravelFollowUpResponse>> add(
			@RequestBody @Valid AddTravelFollowUpRequest request) {
		log.info("[TRAVEL_FOLLOW_UP][ADD] subject={}", request.getSubject());
		return ResponseEntity.ok(ApiResponse.success(service.add(request), ApiMessages.FOLLOW_UP_ADDED));
	}

	@PostMapping("/update")
	@RequireMenuPermission(menuRoute = "/travel/follow-ups", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateTravelFollowUpResponse>> update(
			@RequestBody @Valid UpdateTravelFollowUpRequest request) {
		log.info("[TRAVEL_FOLLOW_UP][UPDATE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.update(request), ApiMessages.FOLLOW_UP_UPDATED));
	}

	@PostMapping("/delete")
	@RequireMenuPermission(menuRoute = "/travel/follow-ups", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteTravelFollowUpResponse>> delete(
			@RequestBody @Valid DeleteTravelFollowUpRequest request) {
		log.info("[TRAVEL_FOLLOW_UP][DELETE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.FOLLOW_UP_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/travel/follow-ups", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetTravelFollowUpResponse>> get(
			@RequestBody @Valid GetTravelFollowUpRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/travel/follow-ups", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetTravelFollowUpResponse>>> gets(
			@RequestBody @Valid GetsTravelFollowUpsRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}
}
