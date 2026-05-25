package com.travel.api.service.invoice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.travel.api.dto.invoice.TravelInvoiceDtos.*;
import com.travel.api.model.TravelInvoice;
import com.travel.api.repository.TravelBookingRepository;
import com.travel.api.repository.TravelInvoiceRepository;
import com.travel.common.ApiMessages;
import com.travel.common.PageResponse;
import com.travel.exception.ServiceException;
import com.travel.security.BusinessContextHolder;

@Service
public class TravelInvoiceServiceImpl implements ITravelInvoiceService {

	@Autowired
	private TravelInvoiceRepository repository;

	@Autowired
	private TravelBookingRepository bookingRepository;

	@Override
	public AddTravelInvoiceResponse add(AddTravelInvoiceRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		validateBooking(businessId, request.getBookingId());
		TravelInvoice e = new TravelInvoice();
		e.setBusinessId(businessId);
		e.setBookingId(request.getBookingId());
		e.setInvoiceNo(request.getInvoiceNo());
		e.setAmount(request.getAmount());
		e.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
		e.setStatus(request.getStatus() != null ? request.getStatus() : "OPEN");
		e.setIssuedAt(request.getIssuedAt() != null ? request.getIssuedAt() : LocalDateTime.now());
		e.setDueAt(request.getDueAt());
		repository.save(e);
		AddTravelInvoiceResponse res = new AddTravelInvoiceResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public UpdateTravelInvoiceResponse update(UpdateTravelInvoiceRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelInvoice e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND));
		validateBooking(businessId, request.getBookingId());
		e.setBookingId(request.getBookingId());
		e.setInvoiceNo(request.getInvoiceNo());
		e.setAmount(request.getAmount());
		if (request.getCurrency() != null) {
			e.setCurrency(request.getCurrency());
		}
		if (request.getStatus() != null) {
			e.setStatus(request.getStatus());
		}
		if (request.getIssuedAt() != null) {
			e.setIssuedAt(request.getIssuedAt());
		}
		e.setDueAt(request.getDueAt());
		repository.save(e);
		UpdateTravelInvoiceResponse res = new UpdateTravelInvoiceResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public DeleteTravelInvoiceResponse delete(DeleteTravelInvoiceRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelInvoice e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long deletedId = e.getId();
		repository.delete(e);
		DeleteTravelInvoiceResponse res = new DeleteTravelInvoiceResponse();
		res.setId(deletedId);
		return res;
	}

	@Override
	public GetTravelInvoiceResponse get(GetTravelInvoiceRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelInvoice e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND));
		return toResponse(e);
	}

	@Override
	public PageResponse<GetTravelInvoiceResponse> gets(GetsTravelInvoicesRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<TravelInvoice> page = repository.findAllByBusinessId(businessId, pageable);
		List<GetTravelInvoiceResponse> items = page.getContent().stream().map(this::toResponse)
				.collect(Collectors.toList());
		PageResponse<GetTravelInvoiceResponse> res = new PageResponse<>();
		res.setItems(items);
		res.setTotalCount(page.getTotalElements());
		res.setPageNumber(page.getNumber());
		res.setPageSize(page.getSize());
		res.setTotalPages(page.getTotalPages());
		return res;
	}

	private void validateBooking(Long businessId, Long bookingId) {
		bookingRepository.findByIdAndBusinessId(bookingId, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.BOOKING_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	private GetTravelInvoiceResponse toResponse(TravelInvoice e) {
		GetTravelInvoiceResponse r = new GetTravelInvoiceResponse();
		r.setId(e.getId());
		r.setBookingId(e.getBookingId());
		r.setInvoiceNo(e.getInvoiceNo());
		r.setAmount(e.getAmount());
		r.setCurrency(e.getCurrency());
		r.setStatus(e.getStatus());
		r.setIssuedAt(e.getIssuedAt());
		r.setDueAt(e.getDueAt());
		return r;
	}
}
