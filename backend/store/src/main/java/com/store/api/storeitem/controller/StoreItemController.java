package com.store.api.storeitem.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.store.api.storeitem.dto.StoreItemDTO;
import com.store.api.storeitem.service.StoreItemService;
import com.store.config.ApiResponse;

@RestController
public class StoreItemController {

	@Autowired
	private StoreItemService storeItemService;

	@GetMapping("/items")
	public ResponseEntity<ApiResponse<List<StoreItemDTO>>> getAllStores() {
		try {
			return ResponseEntity
					.ok(new ApiResponse<>("success", storeItemService.getAllItems(), "Stores retrieved successfully."));
		} catch (Exception e) {
			return ResponseEntity.ok(new ApiResponse<>("fail", null, "An error occurred: " + e.getMessage()));
		}
	}

	@PostMapping("/items")
	public ResponseEntity<ApiResponse<StoreItemDTO>> addStore(@RequestBody StoreItemDTO StoreItemDTO) {
		try {
			return ResponseEntity.ok(new ApiResponse<>("success", storeItemService.addItem(StoreItemDTO),
					"Store created successfully."));
		} catch (Exception e) {
			return ResponseEntity.ok(new ApiResponse<>("fail", null, "An error occurred: " + e.getMessage()));
		}
	}

	@PutMapping("/items/{id}")
	public ResponseEntity<ApiResponse<StoreItemDTO>> updateStore(@PathVariable Long id,
			@Valid @RequestBody StoreItemDTO StoreItemDTO) {
		try {
			return ResponseEntity.ok(new ApiResponse<>("success", storeItemService.updateItem(id, StoreItemDTO),
					"Store updated successfully."));
		} catch (Exception e) {
			return ResponseEntity.ok(new ApiResponse<>("fail", null, "An error occurred: " + e.getMessage()));
		}
	}

	@DeleteMapping("/items/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long id) {
		try {
			storeItemService.deleteItem(id);
			return ResponseEntity.ok(new ApiResponse<>("success", null, "Store deleted successfully."));
		} catch (Exception e) {
			return ResponseEntity.ok(new ApiResponse<>("fail", null, "An error occurred: " + e.getMessage()));
		}
	}

}
