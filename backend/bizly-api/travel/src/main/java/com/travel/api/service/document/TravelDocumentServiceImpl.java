package com.travel.api.service.document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.travel.api.dto.document.TravelDocumentDtos.*;
import com.travel.api.model.TravelDocument;
import com.travel.api.repository.TravelBookingRepository;
import com.travel.api.repository.TravelClientRepository;
import com.travel.api.repository.TravelDocumentRepository;
import com.travel.common.ApiMessages;
import com.travel.common.PageResponse;
import com.travel.exception.ServiceException;
import com.travel.security.BusinessContextHolder;

@Service
public class TravelDocumentServiceImpl implements ITravelDocumentService {

	@Autowired
	private TravelDocumentRepository repository;

	@Autowired
	private TravelClientRepository clientRepository;

	@Autowired
	private TravelBookingRepository bookingRepository;

	@Override
	public AddTravelDocumentResponse add(AddTravelDocumentRequest request, String username) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		validateOptionalRefs(businessId, request.getClientId(), request.getBookingId());
		TravelDocument e = new TravelDocument();
		e.setBusinessId(businessId);
		e.setClientId(request.getClientId());
		e.setBookingId(request.getBookingId());
		e.setDocType(request.getDocType());
		e.setFileName(request.getFileName());
		e.setStorageRef(request.getStorageRef());
		e.setUploadedAt(LocalDateTime.now());
		e.setUploadedBy(request.getUploadedBy() != null ? request.getUploadedBy() : username);
		repository.save(e);
		AddTravelDocumentResponse res = new AddTravelDocumentResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public UpdateTravelDocumentResponse update(UpdateTravelDocumentRequest request, String username) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelDocument e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.DOCUMENT_NOT_FOUND, HttpStatus.NOT_FOUND));
		validateOptionalRefs(businessId, request.getClientId(), request.getBookingId());
		e.setClientId(request.getClientId());
		e.setBookingId(request.getBookingId());
		e.setDocType(request.getDocType());
		e.setFileName(request.getFileName());
		e.setStorageRef(request.getStorageRef());
		if (request.getUploadedBy() != null) {
			e.setUploadedBy(request.getUploadedBy());
		} else if (username != null) {
			e.setUploadedBy(username);
		}
		repository.save(e);
		UpdateTravelDocumentResponse res = new UpdateTravelDocumentResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public DeleteTravelDocumentResponse delete(DeleteTravelDocumentRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelDocument e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.DOCUMENT_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long deletedId = e.getId();
		repository.delete(e);
		DeleteTravelDocumentResponse res = new DeleteTravelDocumentResponse();
		res.setId(deletedId);
		return res;
	}

	@Override
	public GetTravelDocumentResponse get(GetTravelDocumentRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelDocument e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.DOCUMENT_NOT_FOUND, HttpStatus.NOT_FOUND));
		return toResponse(e);
	}

	@Override
	public PageResponse<GetTravelDocumentResponse> gets(GetsTravelDocumentsRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<TravelDocument> page = repository.findAllByBusinessId(businessId, pageable);
		List<GetTravelDocumentResponse> items = page.getContent().stream().map(this::toResponse)
				.collect(Collectors.toList());
		PageResponse<GetTravelDocumentResponse> res = new PageResponse<>();
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

	private GetTravelDocumentResponse toResponse(TravelDocument e) {
		GetTravelDocumentResponse r = new GetTravelDocumentResponse();
		r.setId(e.getId());
		r.setClientId(e.getClientId());
		r.setBookingId(e.getBookingId());
		r.setDocType(e.getDocType());
		r.setFileName(e.getFileName());
		r.setStorageRef(e.getStorageRef());
		r.setUploadedAt(e.getUploadedAt());
		r.setUploadedBy(e.getUploadedBy());
		return r;
	}
}
