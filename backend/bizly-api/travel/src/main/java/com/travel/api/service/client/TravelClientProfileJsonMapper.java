package com.travel.api.service.client;

import com.travel.api.dto.client.TravelClientProfileJson.TravelClientProfileDto;
import com.travel.api.service.common.TravelJsonMapper;

final class TravelClientProfileJsonMapper {

	private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = TravelJsonMapper.get();

	private TravelClientProfileJsonMapper() {}

	static String toJson(TravelClientProfileDto profile) {
		if (profile == null) {
			return null;
		}
		try {
			return MAPPER.writeValueAsString(profile);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid travel profile", e);
		}
	}

	static TravelClientProfileDto fromJson(String json) {
		if (json == null || json.isBlank()) {
			return null;
		}
		try {
			return MAPPER.readValue(json, TravelClientProfileDto.class);
		} catch (Exception e) {
			return null;
		}
	}
}
