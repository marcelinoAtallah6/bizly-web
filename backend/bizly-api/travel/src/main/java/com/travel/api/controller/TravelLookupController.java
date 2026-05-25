package com.travel.api.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.travel.api.dto.lookup.TravelLookupDtos.CountryOptionResponse;
import com.travel.api.dto.lookup.TravelLookupDtos.GetsCountriesRequest;
import java.util.stream.Collectors;

import com.travel.api.model.RefCountry;
import com.travel.api.repository.RefCountryRepository;
import com.travel.common.ApiMessages;
import com.travel.common.ApiResponse;
import com.travel.common.PageResponse;

/**
 * Shared paginated lookups for travel UI dropdowns (countries, etc.).
 */
@RestController
@RequestMapping("/lookup")
public class TravelLookupController {

	@Autowired
	private RefCountryRepository countryRepository;

	@PostMapping("/countries/gets")
	public @ResponseBody ResponseEntity<ApiResponse<PageResponse<CountryOptionResponse>>> getCountries(
			@RequestBody @Valid GetsCountriesRequest request) {
		String q = request.getQuery() != null ? request.getQuery().trim() : "";
		Page<RefCountry> page = countryRepository.searchActive(q,
				PageRequest.of(request.getPageNumber(), request.getPageSize()));
		PageResponse<CountryOptionResponse> res = new PageResponse<>();
		res.setItems(page.getContent().stream().map(this::toCountry).collect(Collectors.toList()));
		res.setTotalCount(page.getTotalElements());
		res.setPageNumber(page.getNumber());
		res.setPageSize(page.getSize());
		res.setTotalPages(page.getTotalPages());
		return ResponseEntity.ok(ApiResponse.success(res, ApiMessages.SUCCESS));
	}

	private CountryOptionResponse toCountry(RefCountry c) {
		CountryOptionResponse r = new CountryOptionResponse();
		r.setId(c.getId());
		r.setIso2(c.getIso2());
		r.setIso3(c.getIso3());
		r.setName(c.getName());
		r.setDialCode(c.getDialCode());
		r.setLabel(c.getName() + " (" + c.getIso2() + ")");
		return r;
	}
}
