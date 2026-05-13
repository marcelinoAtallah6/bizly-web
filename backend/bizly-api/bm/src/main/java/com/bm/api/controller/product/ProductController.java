package com.bm.api.controller.product;

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
import com.bm.api.dto.product.add.AddProductRequest;
import com.bm.api.dto.product.add.AddProductResponse;
import com.bm.api.dto.product.delete.DeleteProductRequest;
import com.bm.api.dto.product.delete.DeleteProductResponse;
import com.bm.api.dto.product.get.GetProductRequest;
import com.bm.api.dto.product.get.GetProductResponse;
import com.bm.audit.Audited;
import com.bm.api.dto.product.gets.GetsProductsRequest;
import com.bm.api.dto.product.update.UpdateProductRequest;
import com.bm.api.dto.product.update.UpdateProductResponse;
import com.bm.api.service.product.IProductService;
import com.bm.security.MenuPermissionAction;
import com.bm.security.RequireMenuPermission;
import com.bm.common.ApiMessages;
import com.bm.common.ApiResponse;
import com.bm.common.PageResponse;


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