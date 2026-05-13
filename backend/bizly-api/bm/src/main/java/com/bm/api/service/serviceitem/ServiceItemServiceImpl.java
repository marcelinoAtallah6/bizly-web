package com.bm.api.service.serviceitem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.bm.api.dto.serviceitem.add.AddServiceItemRequest;
import com.bm.api.dto.serviceitem.add.AddServiceItemResponse;
import com.bm.api.dto.serviceitem.delete.DeactivateServiceItemRequest;
import com.bm.api.dto.serviceitem.delete.DeactivateServiceItemResponse;
import com.bm.api.dto.serviceitem.get.GetServiceItemRequest;
import com.bm.api.dto.serviceitem.get.GetServiceItemResponse;
import com.bm.api.dto.serviceitem.gets.GetsServiceItemsRequest;
import com.bm.api.dto.serviceitem.update.UpdateServiceItemRequest;
import com.bm.api.dto.serviceitem.update.UpdateServiceItemResponse;
import com.bm.api.repository.mapper.ServiceItemDtoMapper;
import com.bm.api.model.ServiceItem;
import com.bm.api.repository.ServiceItemRepository;
import com.bm.common.ApiMessages;
import com.bm.common.PageResponse;
import com.bm.exception.ServiceException;
import com.bm.security.BusinessContextHolder;

@Service
public class ServiceItemServiceImpl implements IServiceItemService {

	@Autowired
	private ServiceItemRepository repository;

	@Override
	public AddServiceItemResponse add(AddServiceItemRequest request, String username) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		ServiceItem e = new ServiceItem();
		e.setBusinessId(businessId);
		e.setName(request.getName());
		e.setDescription(request.getDescription());
		e.setPrice(request.getPrice());
		e.setDurationMinutes(request.getDurationMinutes());
		e.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
		LocalDateTime now = LocalDateTime.now();
		e.setCreatedAt(now);
		e.setUpdatedAt(now);
		e.setCreatedBy(username);
		e.setUpdatedBy(username);

		repository.save(e);

		AddServiceItemResponse res = new AddServiceItemResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public UpdateServiceItemResponse update(UpdateServiceItemRequest request, String username) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		ServiceItem e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.SERVICE_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND));

		e.setName(request.getName());
		e.setDescription(request.getDescription());
		e.setPrice(request.getPrice());
		e.setDurationMinutes(request.getDurationMinutes());
		if (request.getActive() != null) {
			e.setActive(request.getActive());
		}
		e.setUpdatedAt(LocalDateTime.now());
		e.setUpdatedBy(username);

		repository.save(e);
		return new UpdateServiceItemResponse();
	}

	@Override
	public DeactivateServiceItemResponse deactivate(DeactivateServiceItemRequest request, String username) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		ServiceItem e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.SERVICE_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND));

		e.setActive(false);
		e.setUpdatedAt(LocalDateTime.now());
		e.setUpdatedBy(username);
		repository.save(e);
		return new DeactivateServiceItemResponse();
	}

	@Override
	public GetServiceItemResponse get(GetServiceItemRequest request) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		ServiceItem e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.SERVICE_ITEM_NOT_FOUND, HttpStatus.NOT_FOUND));

		return ServiceItemDtoMapper.toResponse(e);
	}

	@Override
	public PageResponse<GetServiceItemResponse> gets(GetsServiceItemsRequest request) {

		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		boolean includeInactive = Boolean.TRUE.equals(request.getIncludeInactive());

		Page<ServiceItem> page = includeInactive
				? repository.findAllByBusinessId(businessId, pageable)
				: repository.findByBusinessIdAndActiveTrue(businessId, pageable);

		List<GetServiceItemResponse> list = page.getContent().stream().map(ServiceItemDtoMapper::toResponse)
				.collect(Collectors.toList());

		PageResponse<GetServiceItemResponse> response = new PageResponse<>();
		response.setItems(list);
		response.setTotalCount(page.getTotalElements());
		response.setPageNumber(page.getNumber());
		response.setPageSize(page.getSize());
		response.setTotalPages(page.getTotalPages());
		return response;
	}
}
