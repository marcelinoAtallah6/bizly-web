package com.travel.api.service.client;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.travel.api.dto.client.TravelClientDtos.*;
import com.travel.api.model.TravelClient;
import com.travel.api.repository.TravelClientRepository;
import com.travel.common.ApiMessages;
import com.travel.common.PageResponse;
import com.travel.exception.ServiceException;
import com.travel.security.BusinessContextHolder;

@Service
public class TravelClientServiceImpl implements ITravelClientService {

	@Autowired
	private TravelClientRepository repository;

	@Override
	public AddTravelClientResponse add(AddTravelClientRequest request, String username) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelClient e = new TravelClient();
		e.setBusinessId(businessId);
		e.setFullName(request.getFullName());
		e.setEmail(request.getEmail());
		e.setPhone(request.getPhone());
		e.setPassportNo(request.getPassportNo());
		e.setNotes(request.getNotes());
		e.setTravelProfileJson(TravelClientProfileJsonMapper.toJson(request.getTravelProfile()));
		e.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
		LocalDateTime now = LocalDateTime.now();
		e.setCreatedAt(now);
		e.setUpdatedAt(now);
		e.setCreatedBy(username);
		e.setUpdatedBy(username);
		repository.save(e);
		AddTravelClientResponse res = new AddTravelClientResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public UpdateTravelClientResponse update(UpdateTravelClientRequest request, String username) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelClient e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.CLIENT_NOT_FOUND, HttpStatus.NOT_FOUND));
		e.setFullName(request.getFullName());
		e.setEmail(request.getEmail());
		e.setPhone(request.getPhone());
		e.setPassportNo(request.getPassportNo());
		e.setNotes(request.getNotes());
		if (request.getTravelProfile() != null) {
			e.setTravelProfileJson(TravelClientProfileJsonMapper.toJson(request.getTravelProfile()));
		}
		if (request.getActive() != null) {
			e.setActive(request.getActive());
		}
		e.setUpdatedAt(LocalDateTime.now());
		e.setUpdatedBy(username);
		repository.save(e);
		UpdateTravelClientResponse res = new UpdateTravelClientResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public DeleteTravelClientResponse delete(DeleteTravelClientRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelClient e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.CLIENT_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long deletedId = e.getId();
		repository.delete(e);
		DeleteTravelClientResponse res = new DeleteTravelClientResponse();
		res.setId(deletedId);
		return res;
	}

	@Override
	public GetTravelClientResponse get(GetTravelClientRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelClient e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.CLIENT_NOT_FOUND, HttpStatus.NOT_FOUND));
		return toResponse(e);
	}

	@Override
	public PageResponse<GetTravelClientResponse> gets(GetsTravelClientsRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<TravelClient> page = repository.findAllByBusinessId(businessId, pageable);
		List<GetTravelClientResponse> items = page.getContent().stream().map(this::toResponse)
				.collect(Collectors.toList());
		PageResponse<GetTravelClientResponse> res = new PageResponse<>();
		res.setItems(items);
		res.setTotalCount(page.getTotalElements());
		res.setPageNumber(page.getNumber());
		res.setPageSize(page.getSize());
		res.setTotalPages(page.getTotalPages());
		return res;
	}

	private GetTravelClientResponse toResponse(TravelClient e) {
		GetTravelClientResponse r = new GetTravelClientResponse();
		r.setId(e.getId());
		r.setFullName(e.getFullName());
		r.setEmail(e.getEmail());
		r.setPhone(e.getPhone());
		r.setPassportNo(e.getPassportNo());
		r.setNotes(e.getNotes());
		r.setTravelProfile(TravelClientProfileJsonMapper.fromJson(e.getTravelProfileJson()));
		r.setActive(e.getActive());
		return r;
	}
}
