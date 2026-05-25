package com.travel.api.dto.lookup;

import com.travel.common.PageRequest;

import lombok.Getter;
import lombok.Setter;

public final class TravelLookupDtos {

	private TravelLookupDtos() {}

	@Getter
	@Setter
	public static class GetsCountriesRequest extends PageRequest {
		private String query;
	}

	@Getter
	@Setter
	public static class CountryOptionResponse {
		private Long id;
		private String iso2;
		private String iso3;
		private String name;
		private String dialCode;
		private String label;
	}
}
