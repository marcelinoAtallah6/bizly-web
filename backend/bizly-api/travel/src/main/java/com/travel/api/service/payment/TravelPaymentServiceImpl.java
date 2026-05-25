package com.travel.api.service.payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.travel.api.dto.payment.TravelPaymentDtos.*;
import com.travel.api.model.TravelPayment;
import com.travel.api.repository.TravelInvoiceRepository;
import com.travel.api.repository.TravelPaymentRepository;
import com.travel.common.ApiMessages;
import com.travel.common.PageResponse;
import com.travel.exception.ServiceException;
import com.travel.security.BusinessContextHolder;

@Service
public class TravelPaymentServiceImpl implements ITravelPaymentService {

	@Autowired
	private TravelPaymentRepository repository;

	@Autowired
	private TravelInvoiceRepository invoiceRepository;

	@Override
	public AddTravelPaymentResponse add(AddTravelPaymentRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		validateInvoice(businessId, request.getInvoiceId());
		TravelPayment e = new TravelPayment();
		e.setBusinessId(businessId);
		e.setInvoiceId(request.getInvoiceId());
		e.setAmount(request.getAmount());
		e.setPaymentMethod(request.getPaymentMethod());
		e.setPaidAt(LocalDateTime.now());
		e.setReferenceNo(request.getReferenceNo());
		repository.save(e);
		AddTravelPaymentResponse res = new AddTravelPaymentResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public UpdateTravelPaymentResponse update(UpdateTravelPaymentRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelPayment e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.PAYMENT_NOT_FOUND, HttpStatus.NOT_FOUND));
		validateInvoice(businessId, request.getInvoiceId());
		e.setInvoiceId(request.getInvoiceId());
		e.setAmount(request.getAmount());
		e.setPaymentMethod(request.getPaymentMethod());
		e.setReferenceNo(request.getReferenceNo());
		repository.save(e);
		UpdateTravelPaymentResponse res = new UpdateTravelPaymentResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public DeleteTravelPaymentResponse delete(DeleteTravelPaymentRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelPayment e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.PAYMENT_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long deletedId = e.getId();
		repository.delete(e);
		DeleteTravelPaymentResponse res = new DeleteTravelPaymentResponse();
		res.setId(deletedId);
		return res;
	}

	@Override
	public GetTravelPaymentResponse get(GetTravelPaymentRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelPayment e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.PAYMENT_NOT_FOUND, HttpStatus.NOT_FOUND));
		return toResponse(e);
	}

	@Override
	public PageResponse<GetTravelPaymentResponse> gets(GetsTravelPaymentsRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<TravelPayment> page = repository.findAllByBusinessId(businessId, pageable);
		List<GetTravelPaymentResponse> items = page.getContent().stream().map(this::toResponse)
				.collect(Collectors.toList());
		PageResponse<GetTravelPaymentResponse> res = new PageResponse<>();
		res.setItems(items);
		res.setTotalCount(page.getTotalElements());
		res.setPageNumber(page.getNumber());
		res.setPageSize(page.getSize());
		res.setTotalPages(page.getTotalPages());
		return res;
	}

	private void validateInvoice(Long businessId, Long invoiceId) {
		invoiceRepository.findByIdAndBusinessId(invoiceId, businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.INVOICE_NOT_FOUND, HttpStatus.NOT_FOUND));
	}

	private GetTravelPaymentResponse toResponse(TravelPayment e) {
		GetTravelPaymentResponse r = new GetTravelPaymentResponse();
		r.setId(e.getId());
		r.setInvoiceId(e.getInvoiceId());
		r.setAmount(e.getAmount());
		r.setPaymentMethod(e.getPaymentMethod());
		r.setPaidAt(e.getPaidAt());
		r.setReferenceNo(e.getReferenceNo());
		return r;
	}
}
