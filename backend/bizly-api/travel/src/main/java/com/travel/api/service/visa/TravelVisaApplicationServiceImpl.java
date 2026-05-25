package com.travel.api.service.visa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.travel.api.dto.visa.TravelVisaDtos.*;
import com.travel.api.model.TravelVisaApplication;
import com.travel.api.repository.TravelBookingRepository;
import com.travel.api.repository.TravelClientRepository;
import com.travel.api.repository.TravelVisaApplicationRepository;
import com.travel.common.ApiMessages;
import com.travel.common.PageResponse;
import com.travel.exception.ServiceException;
import com.travel.security.BusinessContextHolder;

@Service
public class TravelVisaApplicationServiceImpl implements ITravelVisaApplicationService {

	@Autowired
	private TravelVisaApplicationRepository repository;

	@Autowired
	private TravelClientRepository clientRepository;

	@Autowired
	private TravelBookingRepository bookingRepository;

	@Override
	public AddTravelVisaResponse add(AddTravelVisaRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		validateClientAndBooking(businessId, request.getClientId(), request.getBookingId());
		TravelVisaApplication e = new TravelVisaApplication();
		e.setBusinessId(businessId);
		e.setClientId(request.getClientId());
		e.setBookingId(request.getBookingId());
		e.setCountry(request.getCountry());
		e.setVisaType(request.getVisaType());
		e.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
		e.setSubmittedAt(request.getSubmittedAt());
		e.setDecisionAt(request.getDecisionAt());
		e.setNotes(request.getNotes());
		LocalDateTime now = LocalDateTime.now();
		e.setCreatedAt(now);
		e.setUpdatedAt(now);
		repository.save(e);
		AddTravelVisaResponse res = new AddTravelVisaResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public UpdateTravelVisaResponse update(UpdateTravelVisaRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelVisaApplication e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.VISA_NOT_FOUND, HttpStatus.NOT_FOUND));
		validateClientAndBooking(businessId, request.getClientId(), request.getBookingId());
		e.setClientId(request.getClientId());
		e.setBookingId(request.getBookingId());
		e.setCountry(request.getCountry());
		e.setVisaType(request.getVisaType());
		if (request.getStatus() != null) {
			e.setStatus(request.getStatus());
		}
		e.setSubmittedAt(request.getSubmittedAt());
		e.setDecisionAt(request.getDecisionAt());
		e.setNotes(request.getNotes());
		e.setUpdatedAt(LocalDateTime.now());
		repository.save(e);
		UpdateTravelVisaResponse res = new UpdateTravelVisaResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public DeleteTravelVisaResponse delete(DeleteTravelVisaRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelVisaApplication e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.VISA_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long deletedId = e.getId();
		repository.delete(e);
		DeleteTravelVisaResponse res = new DeleteTravelVisaResponse();
		res.setId(deletedId);
		return res;
	}

	@Override
	public GetTravelVisaResponse get(GetTravelVisaRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelVisaApplication e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.VISA_NOT_FOUND, HttpStatus.NOT_FOUND));
		return toResponse(e);
	}

	@Override
	public PageResponse<GetTravelVisaResponse> gets(GetsTravelVisasRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<TravelVisaApplication> page = repository.findAllByBusinessId(businessId, pageable);
		List<GetTravelVisaResponse> items = page.getContent().stream().map(this::toResponse)
				.collect(Collectors.toList());
		PageResponse<GetTravelVisaResponse> res = new PageResponse<>();
		res.setItems(items);
		res.setTotalCount(page.getTotalElements());
		res.setPageNumber(page.getNumber());
		res.setPageSize(page.getSize());
		res.setTotalPages(page.getTotalPages());
		return res;
	}

	private void validateClientAndBooking(Long businessId, Long clientId, Long bookingId) {
		clientRepository.findByIdAndBusinessId(clientId, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.CLIENT_NOT_FOUND, HttpStatus.NOT_FOUND));
		if (bookingId != null) {
			bookingRepository.findByIdAndBusinessId(bookingId, businessId)
					.orElseThrow(() -> new ServiceException(ApiMessages.BOOKING_NOT_FOUND, HttpStatus.NOT_FOUND));
		}
	}

	private GetTravelVisaResponse toResponse(TravelVisaApplication e) {
		GetTravelVisaResponse r = new GetTravelVisaResponse();
		r.setId(e.getId());
		r.setClientId(e.getClientId());
		r.setBookingId(e.getBookingId());
		r.setCountry(e.getCountry());
		r.setVisaType(e.getVisaType());
		r.setStatus(e.getStatus());
		r.setSubmittedAt(e.getSubmittedAt());
		r.setDecisionAt(e.getDecisionAt());
		r.setNotes(e.getNotes());
		return r;
	}
}
