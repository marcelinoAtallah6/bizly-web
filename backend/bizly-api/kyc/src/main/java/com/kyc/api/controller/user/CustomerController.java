package com.kyc.api.controller.user;

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

import com.kyc.api.dto.add.AddCustomerRequest;
import com.kyc.api.dto.add.AddCustomerResponse;
import com.kyc.api.dto.delete.DeleteCustomerRequest;
import com.kyc.api.dto.delete.DeleteCustomerResponse;
import com.kyc.api.dto.get.GetCustomerRequest;
import com.kyc.api.dto.get.GetCustomerResponse;
import com.kyc.audit.Audited;
import com.kyc.api.dto.gets.GetsCustomersRequest;
import com.kyc.api.dto.update.UpdateCustomerRequest;
import com.kyc.api.dto.update.UpdateCustomerResponse;
import com.kyc.api.service.ICustomerService;
import com.kyc.security.MenuPermissionAction;
import com.kyc.security.RequireMenuPermission;
import com.kyc.common.ApiMessages;
import com.kyc.common.ApiResponse;
import com.kyc.common.PageResponse;

@RestController
@RequestMapping("/customer")
public class CustomerController {

	private static final Logger log = LogManager.getLogger(CustomerController.class);

	@Autowired
	private ICustomerService service;

	@PostMapping("/add")
	@Audited(action = "KYC_CUSTOMER_ADD", resourceType = "CUSTOMER")
	@RequireMenuPermission(menuRoute = "/kyc/customers", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddCustomerResponse>> add(
			@RequestBody @Valid AddCustomerRequest request) {

		log.info("[KYC_CUSTOMER][ADD] email={}", request.getEmail());

		return ResponseEntity.ok(ApiResponse.success(service.add(request), ApiMessages.CUSTOMER_ADDED));
	}

	@PostMapping("/update")
	@Audited(action = "KYC_CUSTOMER_UPDATE", resourceType = "CUSTOMER")
	@RequireMenuPermission(menuRoute = "/kyc/customers", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateCustomerResponse>> update(
			@RequestBody @Valid UpdateCustomerRequest request) {

		log.info("[KYC_CUSTOMER][UPDATE] id={}", request.getId());

		return ResponseEntity.ok(ApiResponse.success(service.update(request), ApiMessages.CUSTOMER_UPDATED));
	}

	@PostMapping("/delete")
	@Audited(action = "KYC_CUSTOMER_DELETE", resourceType = "CUSTOMER")
	@RequireMenuPermission(menuRoute = "/kyc/customers", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteCustomerResponse>> delete(
			@RequestBody @Valid DeleteCustomerRequest request) {

		log.info("[KYC_CUSTOMER][DELETE] id={}", request.getId());

		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.CUSTOMER_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/kyc/customers", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetCustomerResponse>> get(
			@RequestBody @Valid GetCustomerRequest request) {

		log.info("[KYC_CUSTOMER][GET] id={}", request.getId());

		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/kyc/customers", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetCustomerResponse>>> gets(
			@RequestBody @Valid GetsCustomersRequest request) {

		log.info("[KYC_CUSTOMER][GETS] page={} size={}", request.getPageNumber(), request.getPageSize());

		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}
}
