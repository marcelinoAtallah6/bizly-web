package com.travel.api.dto.client;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/** Serialized to {@code travel_client.travel_profile_json}. */
public final class TravelClientProfileJson {

	private TravelClientProfileJson() {}

	@Getter
	@Setter
	public static class TravelClientProfileDto {
		private String nationality;
		private String passportIssueDate;
		private String passportExpiryDate;
		private String passportPlaceOfIssue;
		private String passportType;
		private String passportScanStorageRef;
		private String nationalId;
		private List<String> savedDestinations = new ArrayList<>();
		private List<String> preferredAirlines = new ArrayList<>();
		private List<String> preferredHotels = new ArrayList<>();
		private String loyaltyProgram;
		private Integer loyaltyPoints;
		private String loyaltyStatus;
	}
}
