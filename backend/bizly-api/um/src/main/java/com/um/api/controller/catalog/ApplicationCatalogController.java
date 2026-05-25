package com.um.api.controller.catalog;

import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.um.api.dto.catalog.ApplicationCatalogResponse;
import com.um.api.dto.catalog.CatalogIdRequest;
import com.um.api.dto.catalog.CatalogSaveResponse;
import com.um.api.dto.catalog.ReorderCatalogRequest;
import com.um.api.dto.catalog.SaveApplicationCatalogRequest;
import com.um.api.dto.catalog.SaveMenuCatalogRequest;
import com.um.api.service.catalog.IApplicationCatalogService;
import com.um.common.ApiMessages;
import com.um.common.ApiResponse;

@RestController
@RequestMapping("/application-catalog")
public class ApplicationCatalogController {

	private static final Logger log = LogManager.getLogger(ApplicationCatalogController.class);

	@Autowired
	private IApplicationCatalogService catalogService;

	@PostMapping("/catalog")
	public ResponseEntity<ApiResponse<ApplicationCatalogResponse>> catalog() {
		log.info("[UM_APP_CATALOG][CATALOG]");
		return ResponseEntity.ok(ApiResponse.success(catalogService.getCatalog(), ApiMessages.SUCCESS));
	}

	@PostMapping("/application/save")
	public ResponseEntity<ApiResponse<CatalogSaveResponse>> saveApplication(
			@Valid @RequestBody SaveApplicationCatalogRequest request) {
		log.info("[UM_APP_CATALOG][APP_SAVE] id={} name={}", request.getId(), request.getName());
		return ResponseEntity.ok(ApiResponse.success(catalogService.saveApplication(request), "Application saved"));
	}

	@PostMapping("/menu/save")
	public ResponseEntity<ApiResponse<CatalogSaveResponse>> saveMenu(@Valid @RequestBody SaveMenuCatalogRequest request) {
		log.info("[UM_APP_CATALOG][MENU_SAVE] id={} appId={} name={}", request.getId(), request.getApplicationId(),
				request.getName());
		return ResponseEntity.ok(ApiResponse.success(catalogService.saveMenu(request), "Menu saved"));
	}

	@PostMapping("/application/delete")
	public ResponseEntity<ApiResponse<Void>> deleteApplication(@Valid @RequestBody CatalogIdRequest request) {
		log.info("[UM_APP_CATALOG][APP_DELETE] id={}", request.getId());
		catalogService.deleteApplication(request);
		return ResponseEntity.ok(ApiResponse.success(null, "Application deleted"));
	}

	@PostMapping("/menu/delete")
	public ResponseEntity<ApiResponse<Void>> deleteMenu(@Valid @RequestBody CatalogIdRequest request) {
		log.info("[UM_APP_CATALOG][MENU_DELETE] id={}", request.getId());
		catalogService.deleteMenu(request);
		return ResponseEntity.ok(ApiResponse.success(null, "Menu deleted"));
	}

	@PostMapping("/reorder")
	public ResponseEntity<ApiResponse<Void>> reorder(@Valid @RequestBody ReorderCatalogRequest request) {
		log.info("[UM_APP_CATALOG][REORDER] scope={} count={}", request.getScope(),
				request.getOrderedIds() == null ? 0 : request.getOrderedIds().size());
		catalogService.reorder(request);
		return ResponseEntity.ok(ApiResponse.success(null, "Order updated"));
	}
}
