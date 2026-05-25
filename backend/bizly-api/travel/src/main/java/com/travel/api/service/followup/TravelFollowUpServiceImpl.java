package com.travel.api.service.followup;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.travel.api.dto.followup.TravelFollowUpDtos.*;
import com.travel.api.model.TravelFollowUp;
import com.travel.api.repository.TravelBookingRepository;
import com.travel.api.repository.TravelClientRepository;
import com.travel.api.repository.TravelFollowUpRepository;
import com.travel.common.ApiMessages;
import com.travel.common.PageResponse;
import com.travel.exception.ServiceException;
import com.travel.security.BusinessContextHolder;

@Service
public class TravelFollowUpServiceImpl implements ITravelFollowUpService {

	@Autowired
	private TravelFollowUpRepository repository;

	@Autowired
	private TravelClientRepository clientRepository;

	@Autowired
	private TravelBookingRepository bookingRepository;

	@Override
	public AddTravelFollowUpResponse add(AddTravelFollowUpRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		validateOptionalRefs(businessId, request.getClientId(), request.getBookingId());
		TravelFollowUp e = new TravelFollowUp();
		e.setBusinessId(businessId);
		e.setClientId(request.getClientId());
		e.setBookingId(request.getBookingId());
		e.setSubject(request.getSubject());
		e.setDueAt(request.getDueAt());
		e.setStatus(request.getStatus() != null ? request.getStatus() : "OPEN");
		e.setNotes(request.getNotes());
		e.setAssignedTo(request.getAssignedTo());
		e.setCreatedAt(LocalDateTime.now());
		repository.save(e);
		AddTravelFollowUpResponse res = new AddTravelFollowUpResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public UpdateTravelFollowUpResponse update(UpdateTravelFollowUpRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelFollowUp e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.FOLLOW_UP_NOT_FOUND, HttpStatus.NOT_FOUND));
		validateOptionalRefs(businessId, request.getClientId(), request.getBookingId());
		e.setClientId(request.getClientId());
		e.setBookingId(request.getBookingId());
		e.setSubject(request.getSubject());
		e.setDueAt(request.getDueAt());
		if (request.getStatus() != null) {
			e.setStatus(request.getStatus());
		}
		e.setNotes(request.getNotes());
		e.setAssignedTo(request.getAssignedTo());
		repository.save(e);
		UpdateTravelFollowUpResponse res = new UpdateTravelFollowUpResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public DeleteTravelFollowUpResponse delete(DeleteTravelFollowUpRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelFollowUp e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.FOLLOW_UP_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long deletedId = e.getId();
		repository.delete(e);
		DeleteTravelFollowUpResponse res = new DeleteTravelFollowUpResponse();
		res.setId(deletedId);
		return res;
	}

	@Override
	public GetTravelFollowUpResponse get(GetTravelFollowUpRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelFollowUp e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.FOLLOW_UP_NOT_FOUND, HttpStatus.NOT_FOUND));
		return toResponse(e);
	}

	@Override
	public PageResponse<GetTravelFollowUpResponse> gets(GetsTravelFollowUpsRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<TravelFollowUp> page = repository.findAllByBusinessId(businessId, pageable);
		List<GetTravelFollowUpResponse> items = page.getContent().stream().map(this::toResponse)
				.collect(Collectors.toList());
		PageResponse<GetTravelFollowUpResponse> res = new PageResponse<>();
		res.setItems(items);
		res.setTotalCount(page.getTotalElements());
		res.setPageNumber(page.getNumber());
		res.setPageSize(page.getSize());
		res.setTotalPages(page.getTotalPages());
		return res;
	}

	private void validateOptionalRefs(Long businessId, Long clientId, Long bookingId) {
		if (clientId != null) {
			clientRepository.findByIdAndBusinessId(clientId, businessId)
					.orElseThrow(() -> new ServiceException(ApiMessages.CLIENT_NOT_FOUND, HttpStatus.NOT_FOUND));
		}
		if (bookingId != null) {
			bookingRepository.findByIdAndBusinessId(bookingId, businessId)
					.orElseThrow(() -> new ServiceException(ApiMessages.BOOKING_NOT_FOUND, HttpStatus.NOT_FOUND));
		}
	}

	private GetTravelFollowUpResponse toResponse(TravelFollowUp e) {
		GetTravelFollowUpResponse r = new GetTravelFollowUpResponse();
		r.setId(e.getId());
		r.setClientId(e.getClientId());
		r.setBookingId(e.getBookingId());
		r.setSubject(e.getSubject());
		r.setDueAt(e.getDueAt());
		r.setStatus(e.getStatus());
		r.setNotes(e.getNotes());
		r.setAssignedTo(e.getAssignedTo());
		return r;
	}
}
