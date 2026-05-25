package com.travel.api.service.packagepkg;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.travel.api.service.common.TravelJsonMapper;
import com.travel.api.dto.packagepkg.TravelPackageDetailJson.ItineraryDayDto;
import com.travel.api.dto.packagepkg.TravelPackageDetailJson.MediaItemDto;
import com.travel.api.dto.packagepkg.TravelPackageDetailJson.PackageAddonDto;
import com.travel.api.dto.packagepkg.TravelPackageDetailJson.PackageDestinationDto;

final class TravelPackageJsonMapper {

	private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = TravelJsonMapper.get();

	private TravelPackageJsonMapper() {}

	static String toItineraryJson(List<ItineraryDayDto> items) {
		if (items == null || items.isEmpty()) {
			return null;
		}
		try {
			return MAPPER.writeValueAsString(items);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid itinerary", e);
		}
	}

	static String toMediaJson(List<MediaItemDto> items) {
		if (items == null || items.isEmpty()) {
			return null;
		}
		try {
			return MAPPER.writeValueAsString(items);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid media", e);
		}
	}

	static List<ItineraryDayDto> fromItineraryJson(String json) {
		if (json == null || json.isBlank()) {
			return Collections.emptyList();
		}
		try {
			return MAPPER.readValue(json, new TypeReference<List<ItineraryDayDto>>() {});
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	static List<MediaItemDto> fromMediaJson(String json) {
		if (json == null || json.isBlank()) {
			return Collections.emptyList();
		}
		try {
			return MAPPER.readValue(json, new TypeReference<List<MediaItemDto>>() {});
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	static String toDestinationsJson(List<PackageDestinationDto> items) {
		if (items == null || items.isEmpty()) {
			return null;
		}
		try {
			return MAPPER.writeValueAsString(items);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid destinations", e);
		}
	}

	static String toAddonsJson(List<PackageAddonDto> items) {
		if (items == null || items.isEmpty()) {
			return null;
		}
		try {
			return MAPPER.writeValueAsString(items);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid addons", e);
		}
	}

	static List<PackageDestinationDto> fromDestinationsJson(String json) {
		if (json == null || json.isBlank()) {
			return Collections.emptyList();
		}
		try {
			return MAPPER.readValue(json, new TypeReference<List<PackageDestinationDto>>() {});
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	static List<PackageAddonDto> fromAddonsJson(String json) {
		if (json == null || json.isBlank()) {
			return Collections.emptyList();
		}
		try {
			return MAPPER.readValue(json, new TypeReference<List<PackageAddonDto>>() {});
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
}
