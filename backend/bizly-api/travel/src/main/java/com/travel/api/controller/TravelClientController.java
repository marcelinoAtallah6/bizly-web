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

import com.travel.api.dto.client.TravelClientDtos.*;
import com.travel.api.service.client.ITravelClientService;
import com.travel.common.ApiMessages;
import com.travel.common.ApiResponse;
import com.travel.common.PageResponse;
import com.travel.security.MenuPermissionAction;
import com.travel.security.RequireMenuPermission;

@RestController
@RequestMapping("/client")
public class TravelClientController {

	private static final Logger log = LogManager.getLogger(TravelClientController.class);

	@Autowired
	private ITravelClientService service;

	@PostMapping("/add")
	@RequireMenuPermission(menuRoute = "/travel/clients", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddTravelClientResponse>> add(HttpServletRequest httpRequest,
			@RequestBody @Valid AddTravelClientRequest request) {
		log.info("[TRAVEL_CLIENT][ADD] name={}", request.getFullName());
		return ResponseEntity.ok(ApiResponse.success(service.add(request, httpRequest.getHeader("X-User")),
				ApiMessages.CLIENT_ADDED));
	}

	@PostMapping("/update")
	@RequireMenuPermission(menuRoute = "/travel/clients", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateTravelClientResponse>> update(HttpServletRequest httpRequest,
			@RequestBody @Valid UpdateTravelClientRequest request) {
		log.info("[TRAVEL_CLIENT][UPDATE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.update(request, httpRequest.getHeader("X-User")),
				ApiMessages.CLIENT_UPDATED));
	}

	@PostMapping("/delete")
	@RequireMenuPermission(menuRoute = "/travel/clients", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteTravelClientResponse>> delete(
			@RequestBody @Valid DeleteTravelClientRequest request) {
		log.info("[TRAVEL_CLIENT][DELETE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.CLIENT_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/travel/clients", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetTravelClientResponse>> get(
			@RequestBody @Valid GetTravelClientRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/travel/clients", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetTravelClientResponse>>> gets(
			@RequestBody @Valid GetsTravelClientsRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}
}
