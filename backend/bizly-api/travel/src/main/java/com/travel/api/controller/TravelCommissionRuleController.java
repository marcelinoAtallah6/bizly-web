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

import com.travel.api.dto.commission.TravelCommissionRuleDtos.*;
import com.travel.api.service.commission.ITravelCommissionRuleService;
import com.travel.common.ApiMessages;
import com.travel.common.ApiResponse;
import com.travel.common.PageResponse;
import com.travel.security.MenuPermissionAction;
import com.travel.security.RequireMenuPermission;

@RestController
@RequestMapping("/commission-rule")
public class TravelCommissionRuleController {

	private static final Logger log = LogManager.getLogger(TravelCommissionRuleController.class);

	@Autowired
	private ITravelCommissionRuleService service;

	@PostMapping("/add")
	@RequireMenuPermission(menuRoute = "/travel/commissions", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddTravelCommissionRuleResponse>> add(
			@RequestBody @Valid AddTravelCommissionRuleRequest request) {
		log.info("[TRAVEL_COMMISSION_RULE][ADD] name={}", request.getName());
		return ResponseEntity.ok(ApiResponse.success(service.add(request), ApiMessages.COMMISSION_RULE_ADDED));
	}

	@PostMapping("/update")
	@RequireMenuPermission(menuRoute = "/travel/commissions", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateTravelCommissionRuleResponse>> update(
			@RequestBody @Valid UpdateTravelCommissionRuleRequest request) {
		log.info("[TRAVEL_COMMISSION_RULE][UPDATE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.update(request), ApiMessages.COMMISSION_RULE_UPDATED));
	}

	@PostMapping("/delete")
	@RequireMenuPermission(menuRoute = "/travel/commissions", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteTravelCommissionRuleResponse>> delete(
			@RequestBody @Valid DeleteTravelCommissionRuleRequest request) {
		log.info("[TRAVEL_COMMISSION_RULE][DELETE] id={}", request.getId());
		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.COMMISSION_RULE_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/travel/commissions", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetTravelCommissionRuleResponse>> get(
			@RequestBody @Valid GetTravelCommissionRuleRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/travel/commissions", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetTravelCommissionRuleResponse>>> gets(
			@RequestBody @Valid GetsTravelCommissionRulesRequest request) {
		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}
}
