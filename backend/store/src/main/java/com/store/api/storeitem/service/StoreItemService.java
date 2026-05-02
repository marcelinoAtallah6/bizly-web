package com.store.api.storeitem.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store.api.storeconfig.model.StoreBusinessUnit;
import com.store.api.storeconfig.repository.StoreBusinessUnitRepository;
import com.store.api.storeconfig.repository.StoreItemBusinessUnitRepository;
import com.store.api.storeitem.dto.StoreItemDTO;
import com.store.api.storeitem.model.StoreItem;
import com.store.api.storeitem.model.StoreItemBusinessUnit;
import com.store.api.storeitem.repository.StoreItemRepository;

@Service
public class StoreItemService {

	@Autowired
	private StoreItemRepository itemRepository;
	@Autowired
	private StoreBusinessUnitRepository businessUnitRepository;
	@Autowired
	private StoreItemBusinessUnitRepository itemBusinessUnitRepository;

	public List<StoreItemDTO> getAllItems() {
		try {
			return itemRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}

	public StoreItemDTO getItemById(Long id) {
		return itemRepository.findById(id).map(this::convertToDTO)
				.orElseThrow(() -> new RuntimeException("Item not found"));
	}

	public StoreItemDTO addItem(StoreItemDTO itemDTO) {
		
		try {

		StoreItem item = convertToEntity(itemDTO);
		
		System.out.println("itemDTO == "+itemDTO);

		if (itemDTO.getBusinessUnitId() != null) {
			Optional<StoreBusinessUnit> businessUnitOpt = businessUnitRepository.findById(itemDTO.getBusinessUnitId());
			
			System.out.println("businessUnitOpt == "+businessUnitOpt);

			
			if (businessUnitOpt.isPresent()) {
				StoreItemBusinessUnit itemBusinessUnit = new StoreItemBusinessUnit();
				itemBusinessUnit.setItem(item);
				itemBusinessUnit.setBusinessUnit(businessUnitOpt.get());
				itemBusinessUnit.setQuantity(itemDTO.getBsnQuantity() != null ? itemDTO.getBsnQuantity() : 0); 				
				System.out.println("itemBusinessUnit == "+itemBusinessUnit);

				
				itemBusinessUnitRepository.save(itemBusinessUnit);
			} else {
				throw new RuntimeException("Business unit not found with ID: " + itemDTO.getBusinessUnitId());
			}
		}

		return convertToDTO(itemRepository.save(item));
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;

		}
	}

	public StoreItemDTO updateItem(Long id, StoreItemDTO itemDTO) {
		StoreItem existingItem = itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
		existingItem.setName(itemDTO.getName());
		existingItem.setDescription(itemDTO.getDescription());
		existingItem.setPrice(itemDTO.getPrice());
		existingItem.setQuantity(itemDTO.getQuantity());
		existingItem.setCategoryId(itemDTO.getCategoryId());

		return convertToDTO(itemRepository.save(existingItem));
	}

	public void deleteItem(Long id) {
		itemRepository.deleteById(id);
	}

	private StoreItemDTO convertToDTO(StoreItem item) {
		StoreItemDTO dto = new StoreItemDTO();

		dto.setId(item.getId());
		dto.setName(item.getName());
		dto.setDescription(item.getDescription());
		dto.setCategoryId(item.getCategoryId());
		dto.setPrice(item.getPrice());
		dto.setQuantity(item.getQuantity());
		return dto;
	}

	private StoreItem convertToEntity(StoreItemDTO dto) {
		StoreItem item = new StoreItem();
		item.setName(dto.getName());
		item.setDescription(dto.getDescription());
		item.setPrice(dto.getPrice());
		item.setQuantity(dto.getQuantity());
		item.setCategoryId(dto.getCategoryId());

		return item;
	}

}
