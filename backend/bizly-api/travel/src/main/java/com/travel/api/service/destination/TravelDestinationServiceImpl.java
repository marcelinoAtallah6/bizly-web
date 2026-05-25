package com.travel.api.service.destination;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.travel.api.dto.destination.TravelDestinationDtos.*;
import com.travel.api.model.TravelDestination;
import com.travel.api.repository.TravelDestinationRepository;
import com.travel.common.ApiMessages;
import com.travel.common.PageResponse;
import com.travel.exception.ServiceException;
import com.travel.security.BusinessContextHolder;

@Service
public class TravelDestinationServiceImpl implements ITravelDestinationService {

	@Autowired
	private TravelDestinationRepository repository;

	@Override
	public AddTravelDestinationResponse add(AddTravelDestinationRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelDestination e = new TravelDestination();
		e.setBusinessId(businessId);
		applyFields(e, request.getName(), request.getCountry(), request.getRegion(),
				request.getVisaRequirements(), request.getHealthRequirements(),
				request.getHighSeasonNotes(), request.getLowSeasonNotes(),
				request.getTravelWarnings(), request.getRiskLevel(), request.getTravelAdvisory());
		e.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
		e.setCreatedAt(LocalDateTime.now());
		repository.save(e);
		AddTravelDestinationResponse res = new AddTravelDestinationResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public UpdateTravelDestinationResponse update(UpdateTravelDestinationRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelDestination e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.DESTINATION_NOT_FOUND, HttpStatus.NOT_FOUND));
		applyFields(e, request.getName(), request.getCountry(), request.getRegion(),
				request.getVisaRequirements(), request.getHealthRequirements(),
				request.getHighSeasonNotes(), request.getLowSeasonNotes(),
				request.getTravelWarnings(), request.getRiskLevel(), request.getTravelAdvisory());
		if (request.getActive() != null) {
			e.setActive(request.getActive());
		}
		repository.save(e);
		UpdateTravelDestinationResponse res = new UpdateTravelDestinationResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public DeleteTravelDestinationResponse delete(DeleteTravelDestinationRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelDestination e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.DESTINATION_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long id = e.getId();
		repository.delete(e);
		DeleteTravelDestinationResponse res = new DeleteTravelDestinationResponse();
		res.setId(id);
		return res;
	}

	@Override
	public GetTravelDestinationResponse get(GetTravelDestinationRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelDestination e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.DESTINATION_NOT_FOUND, HttpStatus.NOT_FOUND));
		return toResponse(e);
	}

	@Override
	public PageResponse<GetTravelDestinationResponse> gets(GetsTravelDestinationsRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<TravelDestination> page = repository.findAllByBusinessId(businessId, pageable);
		List<GetTravelDestinationResponse> items = page.getContent().stream().map(this::toResponse)
				.collect(Collectors.toList());
		PageResponse<GetTravelDestinationResponse> res = new PageResponse<>();
		res.setItems(items);
		res.setTotalCount(page.getTotalElements());
		res.setPageNumber(page.getNumber());
		res.setPageSize(page.getSize());
		res.setTotalPages(page.getTotalPages());
		return res;
	}

	private void applyFields(TravelDestination e, String name, String country, String region,
			String visaRequirements, String healthRequirements, String highSeasonNotes,
			String lowSeasonNotes, String travelWarnings, String riskLevel, String travelAdvisory) {
		e.setName(name);
		e.setCountry(country);
		e.setRegion(region);
		e.setVisaRequirements(visaRequirements);
		e.setHealthRequirements(healthRequirements);
		e.setHighSeasonNotes(highSeasonNotes);
		e.setLowSeasonNotes(lowSeasonNotes);
		e.setTravelWarnings(travelWarnings);
		e.setRiskLevel(riskLevel);
		e.setTravelAdvisory(travelAdvisory);
	}

	private GetTravelDestinationResponse toResponse(TravelDestination e) {
		GetTravelDestinationResponse r = new GetTravelDestinationResponse();
		r.setId(e.getId());
		r.setName(e.getName());
		r.setCountry(e.getCountry());
		r.setRegion(e.getRegion());
		r.setVisaRequirements(e.getVisaRequirements());
		r.setHealthRequirements(e.getHealthRequirements());
		r.setHighSeasonNotes(e.getHighSeasonNotes());
		r.setLowSeasonNotes(e.getLowSeasonNotes());
		r.setTravelWarnings(e.getTravelWarnings());
		r.setRiskLevel(e.getRiskLevel());
		r.setTravelAdvisory(e.getTravelAdvisory());
		r.setActive(e.getActive());
		return r;
	}
}
