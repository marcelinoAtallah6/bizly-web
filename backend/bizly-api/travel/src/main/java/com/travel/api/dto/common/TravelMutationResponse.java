package com.travel.api.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Base for update/delete responses so Jackson never fails on empty beans.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TravelMutationResponse {

	private Long id;

	public static TravelMutationResponse of(Long id) {
		TravelMutationResponse r = new TravelMutationResponse();
		r.setId(id);
		return r;
	}
}
