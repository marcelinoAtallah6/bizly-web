package com.travel.api.service.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** Shared Jackson mapper for travel JSON columns (LocalDate, LocalDateTime). */
public final class TravelJsonMapper {

	private static final ObjectMapper MAPPER = create();

	private TravelJsonMapper() {}

	public static ObjectMapper get() {
		return MAPPER;
	}

	private static ObjectMapper create() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return mapper;
	}
}
