package com.pm.api.controller.pm;

import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.pm.api.dto.add.AddProductRequest;
import com.pm.api.dto.add.AddProductResponse;
import com.pm.api.dto.delete.DeleteProductRequest;
import com.pm.api.dto.delete.DeleteProductResponse;
import com.pm.api.dto.get.GetProductRequest;
import com.pm.api.dto.get.GetProductResponse;
import com.pm.audit.Audited;
import com.pm.api.dto.gets.GetsProductsRequest;
import com.pm.api.dto.update.UpdateProductRequest;
import com.pm.api.dto.update.UpdateProductResponse;
import com.pm.api.service.IProductService;
import com.pm.security.MenuPermissionAction;
import com.pm.security.RequireMenuPermission;
import com.pm.common.ApiMessages;
import com.pm.common.ApiResponse;
import com.pm.common.PageResponse;


@RestController
@RequestMapping("/product")
public class ProductController {

	private static final Logger log = LogManager.getLogger(ProductController.class);

	@Autowired
	private IProductService service;

	// ================= ADD =================
	@PostMapping("/add")
	@Audited(action = "PM_PRODUCT_ADD", resourceType = "PRODUCT")
	@RequireMenuPermission(menuRoute = "/pm/products", action = MenuPermissionAction.ADD)
	public @ResponseBody ResponseEntity<ApiResponse<AddProductResponse>> add(
			@RequestBody @Valid AddProductRequest request) {

		log.info("[PM_PRODUCT][ADD] name={}", request.getName());

		return ResponseEntity.ok(ApiResponse.success(service.add(request), ApiMessages.PRODUCT_ADDED));
	}

	// ================= UPDATE =================
	@PostMapping("/update")
	@Audited(action = "PM_PRODUCT_UPDATE", resourceType = "PRODUCT")
	@RequireMenuPermission(menuRoute = "/pm/products", action = MenuPermissionAction.EDIT)
	public @ResponseBody ResponseEntity<ApiResponse<UpdateProductResponse>> update(
			@RequestBody @Valid UpdateProductRequest request) {

		log.info("[PM_PRODUCT][UPDATE] id={}", request.getId());

		return ResponseEntity.ok(ApiResponse.success(service.update(request), ApiMessages.PRODUCT_UPDATED));
	}

	// ================= DELETE =================
	@PostMapping("/delete")
	@Audited(action = "PM_PRODUCT_DELETE", resourceType = "PRODUCT")
	@RequireMenuPermission(menuRoute = "/pm/products", action = MenuPermissionAction.DELETE)
	public @ResponseBody ResponseEntity<ApiResponse<DeleteProductResponse>> delete(
			@RequestBody @Valid DeleteProductRequest request) {

		log.info("[PM_PRODUCT][DELETE] id={}", request.getId());

		return ResponseEntity.ok(ApiResponse.success(service.delete(request), ApiMessages.PRODUCT_DELETED));
	}

	@PostMapping("/get")
	@RequireMenuPermission(menuRoute = "/pm/products", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<GetProductResponse>> get(
			@RequestBody @Valid GetProductRequest request) {

		log.info("[PM_PRODUCT][GET] id={}", request.getId());

		return ResponseEntity.ok(ApiResponse.success(service.get(request), ApiMessages.SUCCESS));
	}

	// ================= GET ALL (PAGINATED) =================
	@PostMapping("/gets")
	@RequireMenuPermission(menuRoute = "/pm/products", action = MenuPermissionAction.VIEW)
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<GetProductResponse>>> gets(
			@RequestBody @Valid GetsProductsRequest request) {

		log.info("[PM_PRODUCT][GETS] page={} size={}", request.getPageNumber(), request.getPageSize());

		return ResponseEntity.ok(ApiResponse.success(service.gets(request), ApiMessages.SUCCESS));
	}
}