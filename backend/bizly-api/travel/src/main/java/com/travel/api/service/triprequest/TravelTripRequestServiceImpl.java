package com.travel.api.service.triprequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.travel.api.dto.triprequest.TravelTripRequestDtos.*;
import com.travel.api.model.TravelBooking;
import com.travel.api.model.TravelTripRequest;
import com.travel.api.repository.TravelBookingRepository;
import com.travel.api.repository.TravelClientRepository;
import com.travel.api.repository.TravelDestinationRepository;
import com.travel.api.repository.TravelTripRequestRepository;
import com.travel.common.ApiMessages;
import com.travel.common.PageResponse;
import com.travel.exception.ServiceException;
import com.travel.security.BusinessContextHolder;

@Service
public class TravelTripRequestServiceImpl implements ITravelTripRequestService {

	private static final Set<String> QUOTEABLE = Set.of("PENDING", "QUOTED");
	private static final Set<String> ACCEPTABLE = Set.of("QUOTED");
	private static final Set<String> REJECTABLE = Set.of("PENDING", "QUOTED");
	private static final Set<String> CONVERTIBLE = Set.of("ACCEPTED", "QUOTED");

	@Autowired
	private TravelTripRequestRepository repository;

	@Autowired
	private TravelClientRepository clientRepository;

	@Autowired
	private TravelDestinationRepository destinationRepository;

	@Autowired
	private TravelBookingRepository bookingRepository;

	@Override
	public AddTravelTripRequestResponse add(AddTravelTripRequestRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		validateClient(businessId, request.getClientId());
		validateDestination(businessId, request.getDestinationId());
		TravelTripRequest e = new TravelTripRequest();
		e.setBusinessId(businessId);
		applyFields(e, request);
		e.setStatus("PENDING");
		LocalDateTime now = LocalDateTime.now();
		e.setCreatedAt(now);
		e.setUpdatedAt(now);
		repository.save(e);
		AddTravelTripRequestResponse res = new AddTravelTripRequestResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public UpdateTravelTripRequestResponse update(UpdateTravelTripRequestRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelTripRequest e = loadEditable(businessId, request.getId());
		validateClient(businessId, request.getClientId());
		validateDestination(businessId, request.getDestinationId());
		applyFields(e, request);
		e.setUpdatedAt(LocalDateTime.now());
		repository.save(e);
		UpdateTravelTripRequestResponse res = new UpdateTravelTripRequestResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public DeleteTravelTripRequestResponse delete(DeleteTravelTripRequestRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelTripRequest e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.TRIP_REQUEST_NOT_FOUND, HttpStatus.NOT_FOUND));
		if ("CONVERTED".equals(e.getStatus())) {
			throw new ServiceException(ApiMessages.TRIP_REQUEST_INVALID_STATUS, HttpStatus.BAD_REQUEST);
		}
		Long deletedId = e.getId();
		repository.delete(e);
		DeleteTravelTripRequestResponse res = new DeleteTravelTripRequestResponse();
		res.setId(deletedId);
		return res;
	}

	@Override
	public GetTravelTripRequestResponse get(GetTravelTripRequestRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelTripRequest e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.TRIP_REQUEST_NOT_FOUND, HttpStatus.NOT_FOUND));
		return toResponse(e);
	}

	@Override
	public PageResponse<GetTravelTripRequestResponse> gets(GetsTravelTripRequestsRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<TravelTripRequest> page = repository.findAllByBusinessId(businessId, pageable);
		List<GetTravelTripRequestResponse> items = page.getContent().stream().map(this::toResponse)
				.collect(Collectors.toList());
		PageResponse<GetTravelTripRequestResponse> res = new PageResponse<>();
		res.setItems(items);
		res.setTotalCount(page.getTotalElements());
		res.setPageNumber(page.getNumber());
		res.setPageSize(page.getSize());
		res.setTotalPages(page.getTotalPages());
		return res;
	}

	@Override
	public QuoteTravelTripRequestResponse quote(QuoteTravelTripRequestRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelTripRequest e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.TRIP_REQUEST_NOT_FOUND, HttpStatus.NOT_FOUND));
		requireStatus(e, QUOTEABLE);
		e.setQuotedAmount(request.getQuotedAmount());
		e.setStatus("QUOTED");
		e.setUpdatedAt(LocalDateTime.now());
		repository.save(e);
		return new QuoteTravelTripRequestResponse();
	}

	@Override
	public AcceptTravelTripRequestResponse accept(AcceptTravelTripRequestRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelTripRequest e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.TRIP_REQUEST_NOT_FOUND, HttpStatus.NOT_FOUND));
		requireStatus(e, ACCEPTABLE);
		e.setStatus("ACCEPTED");
		e.setUpdatedAt(LocalDateTime.now());
		repository.save(e);
		return new AcceptTravelTripRequestResponse();
	}

	@Override
	public RejectTravelTripRequestResponse reject(RejectTravelTripRequestRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelTripRequest e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.TRIP_REQUEST_NOT_FOUND, HttpStatus.NOT_FOUND));
		requireStatus(e, REJECTABLE);
		e.setStatus("REJECTED");
		e.setUpdatedAt(LocalDateTime.now());
		repository.save(e);
		return new RejectTravelTripRequestResponse();
	}

	@Override
	@Transactional
	public ConvertTravelTripRequestResponse convert(ConvertTravelTripRequestRequest request, String username) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelTripRequest e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.TRIP_REQUEST_NOT_FOUND, HttpStatus.NOT_FOUND));
		requireStatus(e, CONVERTIBLE);
		if (e.getBookingId() != null) {
			throw new ServiceException(ApiMessages.TRIP_REQUEST_INVALID_STATUS, HttpStatus.BAD_REQUEST);
		}

		TravelBooking booking = new TravelBooking();
		booking.setBusinessId(businessId);
		booking.setClientId(e.getClientId());
		booking.setStatus("DRAFT");
		booking.setDepartureDate(e.getDepartureDate());
		booking.setReturnDate(e.getReturnDate());
		Double amount = e.getQuotedAmount() != null ? e.getQuotedAmount() : e.getBudgetAmount();
		booking.setTotalAmount(amount);
		booking.setCurrency(e.getCurrency() != null ? e.getCurrency() : "USD");
		booking.setNotes(e.getNotes());
		LocalDateTime now = LocalDateTime.now();
		booking.setCreatedAt(now);
		booking.setUpdatedAt(now);
		booking.setCreatedBy(username);
		booking.setUpdatedBy(username);
		bookingRepository.save(booking);

		e.setBookingId(booking.getId());
		e.setStatus("CONVERTED");
		e.setUpdatedAt(now);
		repository.save(e);

		ConvertTravelTripRequestResponse res = new ConvertTravelTripRequestResponse();
		res.setBookingId(booking.getId());
		return res;
	}

	private TravelTripRequest loadEditable(Long businessId, Long id) {
		TravelTripRequest e = repository.findByIdAndBusinessId(id, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.TRIP_REQUEST_NOT_FOUND, HttpStatus.NOT_FOUND));
		if ("CONVERTED".equals(e.getStatus()) || "REJECTED".equals(e.getStatus())) {
			throw new ServiceException(ApiMessages.TRIP_REQUEST_INVALID_STATUS, HttpStatus.BAD_REQUEST);
		}
		return e;
	}

	private void validateClient(Long businessId, Long clientId) {
		clientRepository.findByIdAndBusinessId(clientId, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.CLIENT_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	private void validateDestination(Long businessId, Long destinationId) {
		if (destinationId == null) {
			return;
		}
		destinationRepository.findByIdAndBusinessId(destinationId, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.DESTINATION_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	private void requireStatus(TravelTripRequest e, Set<String> allowed) {
		String status = e.getStatus() != null ? e.getStatus().toUpperCase() : "";
		if (!allowed.contains(status)) {
			throw new ServiceException(ApiMessages.TRIP_REQUEST_INVALID_STATUS, HttpStatus.BAD_REQUEST);
		}
	}

	private void applyFields(TravelTripRequest e, AddTravelTripRequestRequest request) {
		e.setClientId(request.getClientId());
		e.setDestinationId(request.getDestinationId());
		e.setDestinationName(request.getDestinationName());
		e.setDepartureDate(request.getDepartureDate());
		e.setReturnDate(request.getReturnDate());
		e.setBudgetAmount(request.getBudgetAmount());
		e.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
		e.setTravelerCount(request.getTravelerCount());
		e.setTravelerDetails(request.getTravelerDetails());
		e.setNotes(request.getNotes());
		e.setAssignedTo(request.getAssignedTo());
	}

	private void applyFields(TravelTripRequest e, UpdateTravelTripRequestRequest request) {
		e.setClientId(request.getClientId());
		e.setDestinationId(request.getDestinationId());
		e.setDestinationName(request.getDestinationName());
		e.setDepartureDate(request.getDepartureDate());
		e.setReturnDate(request.getReturnDate());
		e.setBudgetAmount(request.getBudgetAmount());
		if (request.getCurrency() != null) {
			e.setCurrency(request.getCurrency());
		}
		e.setTravelerCount(request.getTravelerCount());
		e.setTravelerDetails(request.getTravelerDetails());
		e.setNotes(request.getNotes());
		e.setAssignedTo(request.getAssignedTo());
	}

	private GetTravelTripRequestResponse toResponse(TravelTripRequest e) {
		GetTravelTripRequestResponse r = new GetTravelTripRequestResponse();
		r.setId(e.getId());
		r.setClientId(e.getClientId());
		r.setDestinationId(e.getDestinationId());
		r.setDestinationName(e.getDestinationName());
		r.setDepartureDate(e.getDepartureDate());
		r.setReturnDate(e.getReturnDate());
		r.setBudgetAmount(e.getBudgetAmount());
		r.setCurrency(e.getCurrency());
		r.setTravelerCount(e.getTravelerCount());
		r.setTravelerDetails(e.getTravelerDetails());
		r.setStatus(e.getStatus());
		r.setQuotedAmount(e.getQuotedAmount());
		r.setNotes(e.getNotes());
		r.setAssignedTo(e.getAssignedTo());
		r.setBookingId(e.getBookingId());
		return r;
	}
}
