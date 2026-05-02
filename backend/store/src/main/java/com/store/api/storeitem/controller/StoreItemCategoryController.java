package com.store.api.storeitem.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.store.api.storeitem.dto.StoreItemCategoryDTO;
import com.store.api.storeitem.service.StoreItemCategoryService;
import com.store.config.ApiResponse;

@RestController
public class StoreItemCategoryController {

	@Autowired
	private StoreItemCategoryService itemCategoryService;

	// ItemCategory APIs
	@GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<StoreItemCategoryDTO>>> getAllCategories() {
        try {
            return ResponseEntity.ok(new ApiResponse<>("success", itemCategoryService.getAllCategories(), "Categories retrieved successfully."));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>("fail", null, "An error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<StoreItemCategoryDTO>> addCategory(@RequestBody StoreItemCategoryDTO categoryDTO) {
        try {
            return ResponseEntity.ok(new ApiResponse<>("success", itemCategoryService.addCategory(categoryDTO), "Category created successfully."));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>("fail", null, "An error occurred: " + e.getMessage()));
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<StoreItemCategoryDTO>> updateCategory(@PathVariable Long id, @RequestBody StoreItemCategoryDTO categoryDTO) {
        try {
            return ResponseEntity.ok(new ApiResponse<>("success", itemCategoryService.updateCategory(id, categoryDTO), "Category updated successfully."));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>("fail", null, "An error occurred: " + e.getMessage()));
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        try {
            itemCategoryService.deleteCategory(id);
            return ResponseEntity.ok(new ApiResponse<>("success", null, "Category deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse<>("fail", null, "An error occurred: " + e.getMessage()));
        }
    }

}
