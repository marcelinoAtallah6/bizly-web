package com.travel.api.service.booking;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.travel.api.service.common.TravelJsonMapper;
import com.travel.api.dto.packagepkg.TravelPackageDetailJson.PaymentScheduleItemDto;

final class TravelBookingJsonMapper {

	private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER = TravelJsonMapper.get();

	private TravelBookingJsonMapper() {}

	static String toPaymentScheduleJson(List<PaymentScheduleItemDto> items) {
		if (items == null || items.isEmpty()) {
			return null;
		}
		try {
			return MAPPER.writeValueAsString(items);
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid payment schedule", e);
		}
	}

	static List<PaymentScheduleItemDto> fromPaymentScheduleJson(String json) {
		if (json == null || json.isBlank()) {
			return Collections.emptyList();
		}
		try {
			return MAPPER.readValue(json, new TypeReference<List<PaymentScheduleItemDto>>() {});
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
}
