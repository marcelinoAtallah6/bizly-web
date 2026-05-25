package com.travel.api.service.supplier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.travel.api.dto.supplier.TravelSupplierDtos.*;
import com.travel.api.model.TravelSupplier;
import com.travel.api.repository.TravelSupplierRepository;
import com.travel.common.ApiMessages;
import com.travel.common.PageResponse;
import com.travel.exception.ServiceException;
import com.travel.security.BusinessContextHolder;

@Service
public class TravelSupplierServiceImpl implements ITravelSupplierService {

	@Autowired
	private TravelSupplierRepository repository;

	@Override
	public AddTravelSupplierResponse add(AddTravelSupplierRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelSupplier e = new TravelSupplier();
		e.setBusinessId(businessId);
		e.setName(request.getName());
		e.setSupplierType(request.getSupplierType());
		e.setContactEmail(request.getContactEmail());
		e.setContactPhone(request.getContactPhone());
		e.setContractsJson(request.getContractsJson());
		e.setCommissionNotes(request.getCommissionNotes());
		e.setRateTableNotes(request.getRateTableNotes());
		e.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
		e.setCreatedAt(LocalDateTime.now());
		repository.save(e);
		AddTravelSupplierResponse res = new AddTravelSupplierResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public UpdateTravelSupplierResponse update(UpdateTravelSupplierRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelSupplier e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.SUPPLIER_NOT_FOUND, HttpStatus.NOT_FOUND));
		e.setName(request.getName());
		e.setSupplierType(request.getSupplierType());
		e.setContactEmail(request.getContactEmail());
		e.setContactPhone(request.getContactPhone());
		e.setContractsJson(request.getContractsJson());
		e.setCommissionNotes(request.getCommissionNotes());
		e.setRateTableNotes(request.getRateTableNotes());
		if (request.getActive() != null) {
			e.setActive(request.getActive());
		}
		repository.save(e);
		UpdateTravelSupplierResponse res = new UpdateTravelSupplierResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public DeleteTravelSupplierResponse delete(DeleteTravelSupplierRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelSupplier e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.SUPPLIER_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long deletedId = e.getId();
		repository.delete(e);
		DeleteTravelSupplierResponse res = new DeleteTravelSupplierResponse();
		res.setId(deletedId);
		return res;
	}

	@Override
	public GetTravelSupplierResponse get(GetTravelSupplierRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelSupplier e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.SUPPLIER_NOT_FOUND, HttpStatus.NOT_FOUND));
		return toResponse(e);
	}

	@Override
	public PageResponse<GetTravelSupplierResponse> gets(GetsTravelSuppliersRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<TravelSupplier> page = repository.findAllByBusinessId(businessId, pageable);
		List<GetTravelSupplierResponse> items = page.getContent().stream().map(this::toResponse)
				.collect(Collectors.toList());
		PageResponse<GetTravelSupplierResponse> res = new PageResponse<>();
		res.setItems(items);
		res.setTotalCount(page.getTotalElements());
		res.setPageNumber(page.getNumber());
		res.setPageSize(page.getSize());
		res.setTotalPages(page.getTotalPages());
		return res;
	}

	private GetTravelSupplierResponse toResponse(TravelSupplier e) {
		GetTravelSupplierResponse r = new GetTravelSupplierResponse();
		r.setId(e.getId());
		r.setName(e.getName());
		r.setSupplierType(e.getSupplierType());
		r.setContactEmail(e.getContactEmail());
		r.setContactPhone(e.getContactPhone());
		r.setContractsJson(e.getContractsJson());
		r.setCommissionNotes(e.getCommissionNotes());
		r.setRateTableNotes(e.getRateTableNotes());
		r.setActive(e.getActive());
		return r;
	}
}
