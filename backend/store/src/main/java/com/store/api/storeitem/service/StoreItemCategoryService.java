package com.store.api.storeitem.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.api.storeitem.dto.StoreItemCategoryDTO;
import com.store.api.storeitem.model.StoreItemCategory;
import com.store.api.storeitem.repository.StoreItemCategoryRepository;
import com.store.api.storeitem.repository.StoreItemRepository;

@Service
public class StoreItemCategoryService {

	@Autowired
	private StoreItemCategoryRepository categoryRepository;

	// Category methods
	public List<StoreItemCategoryDTO> getAllCategories() {
		return categoryRepository.findAll().stream().map(this::mapToCategoryDTO).collect(Collectors.toList());
	}

	public StoreItemCategoryDTO addCategory(StoreItemCategoryDTO dto) {
		StoreItemCategory category = mapToCategoryEntity(dto);
		return mapToCategoryDTO(categoryRepository.save(category));
	}

	public StoreItemCategoryDTO updateCategory(Long id, StoreItemCategoryDTO dto) {
		StoreItemCategory category = categoryRepository.findById(id).orElseThrow();
		category.setName(dto.getName());
		category.setDescription(dto.getDescription());
		return mapToCategoryDTO(categoryRepository.save(category));
	}

	public void deleteCategory(Long id) {
		categoryRepository.deleteById(id);
	}

	// Other CRUD operations (Items, Sales, Suppliers) follow the same pattern

	private StoreItemCategoryDTO mapToCategoryDTO(StoreItemCategory category) {
		StoreItemCategoryDTO dto = new StoreItemCategoryDTO();
		dto.setId(category.getId());
		dto.setName(category.getName());
		dto.setDescription(category.getDescription());
		return dto;
	}

	private StoreItemCategory mapToCategoryEntity(StoreItemCategoryDTO dto) {
		StoreItemCategory category = new StoreItemCategory();
		category.setName(dto.getName());
		category.setDescription(dto.getDescription());
		return category;
	}
}
