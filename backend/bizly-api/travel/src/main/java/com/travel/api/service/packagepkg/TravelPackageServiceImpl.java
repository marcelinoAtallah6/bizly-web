package com.travel.api.service.packagepkg;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.travel.api.dto.packagepkg.TravelPackageDtos.*;
import com.travel.api.model.TravelPackage;
import com.travel.api.repository.TravelPackageRepository;
import com.travel.common.ApiMessages;
import com.travel.common.PageResponse;
import com.travel.exception.ServiceException;
import com.travel.security.BusinessContextHolder;

@Service
public class TravelPackageServiceImpl implements ITravelPackageService {

	@Autowired
	private TravelPackageRepository repository;

	@Override
	public AddTravelPackageResponse add(AddTravelPackageRequest request, String username) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelPackage e = new TravelPackage();
		e.setBusinessId(businessId);
		e.setCode(request.getCode());
		e.setName(request.getName());
		e.setDescription(request.getDescription());
		applyDetails(e, request.getInclusions(), request.getExclusions(), request.getItinerary(), request.getMedia(),
				request.getDestinations(), request.getAddons());
		e.setMaxCapacityPerDay(request.getMaxCapacityPerDay());
		e.setDestination(request.getDestination());
		e.setDurationDays(request.getDurationDays());
		e.setBasePrice(request.getBasePrice());
		e.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
		e.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
		LocalDateTime now = LocalDateTime.now();
		e.setCreatedAt(now);
		e.setUpdatedAt(now);
		e.setCreatedBy(username);
		e.setUpdatedBy(username);
		repository.save(e);
		AddTravelPackageResponse res = new AddTravelPackageResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public UpdateTravelPackageResponse update(UpdateTravelPackageRequest request, String username) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelPackage e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.PACKAGE_NOT_FOUND, HttpStatus.NOT_FOUND));
		e.setCode(request.getCode());
		e.setName(request.getName());
		e.setDescription(request.getDescription());
		applyDetails(e, request.getInclusions(), request.getExclusions(), request.getItinerary(), request.getMedia(),
				request.getDestinations(), request.getAddons());
		e.setMaxCapacityPerDay(request.getMaxCapacityPerDay());
		e.setDestination(request.getDestination());
		e.setDurationDays(request.getDurationDays());
		e.setBasePrice(request.getBasePrice());
		if (request.getCurrency() != null) {
			e.setCurrency(request.getCurrency());
		}
		if (request.getActive() != null) {
			e.setActive(request.getActive());
		}
		e.setUpdatedAt(LocalDateTime.now());
		e.setUpdatedBy(username);
		repository.save(e);
		UpdateTravelPackageResponse res = new UpdateTravelPackageResponse();
		res.setId(e.getId());
		return res;
	}

	@Override
	public DeleteTravelPackageResponse delete(DeleteTravelPackageRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelPackage e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.PACKAGE_NOT_FOUND, HttpStatus.NOT_FOUND));
		Long deletedId = e.getId();
		repository.delete(e);
		DeleteTravelPackageResponse res = new DeleteTravelPackageResponse();
		res.setId(deletedId);
		return res;
	}

	@Override
	public GetTravelPackageResponse get(GetTravelPackageRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		TravelPackage e = repository.findByIdAndBusinessId(request.getId(), businessId)
				.orElseThrow(() -> new ServiceException(ApiMessages.PACKAGE_NOT_FOUND, HttpStatus.NOT_FOUND));
		return toResponse(e);
	}

	@Override
	public PageResponse<GetTravelPackageResponse> gets(GetsTravelPackagesRequest request) {
		Long businessId = BusinessContextHolder.requireBusinessId();
		Pageable pageable = PageRequest.of(request.getPageNumber(), request.getPageSize());
		Page<TravelPackage> page = repository.findAllByBusinessId(businessId, pageable);
		List<GetTravelPackageResponse> items = page.getContent().stream().map(this::toResponse)
				.collect(Collectors.toList());
		PageResponse<GetTravelPackageResponse> res = new PageResponse<>();
		res.setItems(items);
		res.setTotalCount(page.getTotalElements());
		res.setPageNumber(page.getNumber());
		res.setPageSize(page.getSize());
		res.setTotalPages(page.getTotalPages());
		return res;
	}

	private void applyDetails(TravelPackage e, String inclusions, String exclusions,
			java.util.List<com.travel.api.dto.packagepkg.TravelPackageDetailJson.ItineraryDayDto> itinerary,
			java.util.List<com.travel.api.dto.packagepkg.TravelPackageDetailJson.MediaItemDto> media,
			java.util.List<com.travel.api.dto.packagepkg.TravelPackageDetailJson.PackageDestinationDto> destinations,
			java.util.List<com.travel.api.dto.packagepkg.TravelPackageDetailJson.PackageAddonDto> addons) {
		e.setInclusions(inclusions);
		e.setExclusions(exclusions);
		e.setItineraryJson(TravelPackageJsonMapper.toItineraryJson(itinerary));
		e.setMediaJson(TravelPackageJsonMapper.toMediaJson(media));
		e.setDestinationsJson(TravelPackageJsonMapper.toDestinationsJson(destinations));
		e.setAddonsJson(TravelPackageJsonMapper.toAddonsJson(addons));
	}

	private GetTravelPackageResponse toResponse(TravelPackage e) {
		GetTravelPackageResponse r = new GetTravelPackageResponse();
		r.setId(e.getId());
		r.setCode(e.getCode());
		r.setName(e.getName());
		r.setDescription(e.getDescription());
		r.setInclusions(e.getInclusions());
		r.setExclusions(e.getExclusions());
		r.setItinerary(TravelPackageJsonMapper.fromItineraryJson(e.getItineraryJson()));
		r.setMedia(TravelPackageJsonMapper.fromMediaJson(e.getMediaJson()));
		r.setDestinations(TravelPackageJsonMapper.fromDestinationsJson(e.getDestinationsJson()));
		r.setAddons(TravelPackageJsonMapper.fromAddonsJson(e.getAddonsJson()));
		r.setMaxCapacityPerDay(e.getMaxCapacityPerDay());
		r.setDestination(e.getDestination());
		r.setDurationDays(e.getDurationDays());
		r.setBasePrice(e.getBasePrice());
		r.setCurrency(e.getCurrency());
		r.setActive(e.getActive());
		return r;
	}
}
